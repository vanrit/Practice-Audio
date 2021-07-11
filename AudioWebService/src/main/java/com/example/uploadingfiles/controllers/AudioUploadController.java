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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.uploadingfiles.exceptions.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@RestController
public class AudioUploadController {

	private static Set<String> formats = new HashSet<String>(Arrays.asList("mp3", "aac", "flac",
			"m4a", "m4p", "m4b", "mp4", "3gp", "adx", "aif", "aiff", "aifc", "ape", "asf"));

	@javax.annotation.Resource(name = "authenticationManager")
	private AuthenticationManager authenticationManager;

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
										   @AuthenticationPrincipal CustomUserDetails userDetails) {

		int status = 1;

		AudioFile audioFile = new AudioFile();

		if (checkFormat(file.getOriginalFilename())) {
			try {
				storageService.store(file);

				if (audioRepository.findByFileNameAndUsername(userDetails.getUsername(), file.getOriginalFilename()) == null) {
					audioFile = new AudioFile();
					audioFile.setFileName(file.getOriginalFilename());
					audioFile.setUsername(((CustomUserDetails) SecurityContextHolder.getContext()
							.getAuthentication().getPrincipal()).getUsername());

					audioRepository.save(audioFile);
				}
			} catch (Exception exception) {
				System.err.println(exception.getMessage());
				status = 0;
			}
		}
		else {
			status = -1;
		}

		if (status == 1) {
			return new UploadResponse(file.getOriginalFilename(),
					MvcUriComponentsBuilder.fromMethodName(AudioUploadController.class,
							"serveFile", file.getOriginalFilename()).build().toUri().toString(),
					file.getSize(), status, audioFile.getAudioId());
		} else {
			return new UploadResponse(file.getOriginalFilename(),
					"",
					file.getSize(), status, (long) 0);
		}
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

	@PostMapping("/login")
	public void login(@RequestParam("username") final String username,
					  @RequestParam("password") final String password,
					  final HttpServletRequest request){

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);

		Authentication authentication = authenticationManager.authenticate(authToken);
		SecurityContext securityContext = SecurityContextHolder.getContext();
		securityContext.setAuthentication(authentication);

		HttpSession session = request.getSession(true);
		session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
	}

	@DeleteMapping("/audios/delete")
	public ResponseEntity<?> deleteAudio(@RequestParam("id") Long id) {
		try {
			audioRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

	public boolean checkFormat(String fileName) {
		String fileFormat = fileName.substring(fileName.indexOf('.') + 1);

		return formats.stream().anyMatch(a -> a.equals(fileFormat));
	}

}
