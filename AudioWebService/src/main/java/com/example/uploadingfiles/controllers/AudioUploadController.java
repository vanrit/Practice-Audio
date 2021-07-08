package com.example.uploadingfiles.controllers;

import com.example.uploadingfiles.repository.AudioRepository;
import com.example.uploadingfiles.responses.RegistrationResponse;
import com.example.uploadingfiles.responses.UploadResponse;
import com.example.uploadingfiles.repository.UsersRepository;
import com.example.uploadingfiles.user.AudioFile;
import com.example.uploadingfiles.user.CustomUserDetails;
import com.example.uploadingfiles.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.uploadingfiles.exceptions.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;

@RestController
public class AudioUploadController {

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private AudioRepository audioRepository;

	private final StorageService storageService;

	@Autowired
	public AudioUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/audios/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		if (audioRepository.findByFileNameAndUsername(((CustomUserDetails)SecurityContextHolder
				.getContext().getAuthentication().getPrincipal()).getUsername(), filename) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		Resource file = storageService.loadAsResource(filename);

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	@PostMapping("/audios/upload")
	public UploadResponse handleFileUpload(@RequestParam("file") MultipartFile file,
										   RedirectAttributes redirectAttributes,
										   @AuthenticationPrincipal CustomUserDetails userDetails) {

		storageService.store(file);

		if (audioRepository.findByFileNameAndUsername(userDetails.getUsername(), file.getOriginalFilename()) == null) {
			AudioFile audioFile = new AudioFile();
			audioFile.setFileName(file.getOriginalFilename());
			audioFile.setUsername(((CustomUserDetails) SecurityContextHolder.getContext()
					.getAuthentication().getPrincipal()).getUsername());

			audioRepository.save(audioFile);
		}

		/*redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");*/

		return new UploadResponse(file.getOriginalFilename(),
				MvcUriComponentsBuilder.fromMethodName(AudioUploadController.class,
						"serveFile", file.getOriginalFilename()).build().toUri().toString(),
				file.getSize());
	}

	@PostMapping("/signup")
	public RegistrationResponse processRegister(@RequestBody User user) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);

		try {
			usersRepository.save(user);
		} catch (Exception ex) {
			return new RegistrationResponse(user.getUsername(), "User already exist!");
		}

		return new RegistrationResponse(user.getUsername(), "User registered!");
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
