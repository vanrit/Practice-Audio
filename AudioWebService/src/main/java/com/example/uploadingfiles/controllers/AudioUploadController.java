package com.example.uploadingfiles.controllers;

import com.example.uploadingfiles.repository.AudioRepository;
import com.example.uploadingfiles.responses.RegistrationResponse;
import com.example.uploadingfiles.responses.UploadResponse;
import com.example.uploadingfiles.repository.UsersRepository;
import com.example.uploadingfiles.service.AudioStorageService;
import com.example.uploadingfiles.storage.StorageProperties;
import com.example.uploadingfiles.user.AudioFile;
import com.example.uploadingfiles.user.CustomUserDetails;
import com.example.uploadingfiles.user.User;
import org.apache.coyote.Response;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp4.atom.Mp4HdlrBox;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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

import com.example.uploadingfiles.exceptions.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class AudioUploadController {

	private static Set<String> formats = new HashSet<String>(Arrays.asList("ogg", "mp3", "flac", "mp4",
			"m4a", "m4p", "wma", "wav", "ra", "rm", "m4b", "aif"));

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

	@GetMapping("/")
	@ResponseBody
	public String getMethods() {

		String answer = "Доступные методы:\n" +
				"1) 84.201.143.25:8080/all\n" +
				"* Тип запроса: GET\n" +
				"* Ответ: JSON, содержащий список всех аудиофайлов, с описанием их полей(record_id, user_id, path, record_name, duration, source, source_id, source_author)\n" +
				"2) 84.201.143.25:8080/audios/*filename*\n" +
				"* Тип запроса: GET\n" +
				"* Ответ: аудиофайл\n" +
				"3) 84.201.143.25:8080/audios/upload\n" +
				"* Тип запроса: POST\n" +
				"* Тело запроса:\n" +
				"	1. Поле file, содержащее аудиофайл\n" +
				"	2. Поле source, содержащее информацию об источнике аудиозаписи (Local, Telegram, Whatsapp)\n" +
				"	3. (Опционально) Поле source_author, содержащее имя автора аудиозаписи\n" +
				"	4. (Опционально) Поле source_id, содержащее id автора, если он является зарегистрированным пользователем\n" +
				"* Ответ: JSON, содержащий информацию о результате загрузки (fileName, songId, fileDownloadUri, size, status (1 - загрузка выполнена успешно, 0 - ошибка во время сохранения файла, -1 - некорректный формат файла/файл с таким именем уже загружен))\n" +
				"* Поддерживаемые форматы: ogg, mp3, flac, mp4, m4a, m4p, wma, wav, ra, rm, m4b, aif\n" +
				"4) 84.201.143.25:8080/signup\n" +
				"* Тип запроса: POST\n" +
				"* Тело запроса: JSON, содержащий поля:\n" +
				"	1. firstName - имя пользователя\n" +
				"	2. lastName - фамилия пользователя\n" +
				"	3. username - логин пользователя\n" +
				"	4. password - пароль пользователя\n" +
				"* Ответ: JSON, содержащий поля:\n" +
				"	1. username - имя пользователя\n" +
				"	2. id - id пользователя (0 - в случае неудачной регистрации)\n" +
				"	3. status - статус регистрации:\n" +
				"		- User registered! - пользователь успешно зарегистрирован!\n" +
				"		- User already exist! - пользователь с указанным логином уже существует\n" +
				"		- Registration error! - ошибка во время регистрации\n" +
				"5) 84.201.143.25:8080/login\n" +
				"* Тип запроса: POST\n" +
				"* Тело запроса:\n" +
				"	1. Поле username - логин пользователя\n" +
				"	2. Поле password - пароль пользователя\n" +
				"* Ответ: Код 200 в случае успешной регистрации, 401 - в случае ошибочных данных\n" +
				"6) 84.201.143.25:8080/audios/delete\n" +
				"* Тип запроса: DELETE\n" +
				"* Тело запроса: Поле id, содержащее id аудиофайла для удаления\n" +
				"* Ответ: Код 200, в случае успешного удаления, 404 - файл с указанным id отсутствует на сервере\n" +
				"7) 84.201.143.25:8080/logout\n" +
				"* Тип запроса: Любой\n" +
				"* Ответ: Код 200, в случае успешного выхода из аккаунта\n";

		return answer;
	}

	@GetMapping("/audios/all")
	@ResponseBody
	public List<AudioFile> getUserAudios(@AuthenticationPrincipal CustomUserDetails userDetails) {

		User user = usersRepository.findByUsername(userDetails.getUsername());

		return audioRepository.findAllByUserId(user.getUserId());

	}

	@GetMapping("/audios/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		String username = ((CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
		User user = usersRepository.findByUsername(username);

		if (audioRepository.findBydUserIdAndFileName(user.getUserId(), filename) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		Resource file;

		try {
			file = storageService.loadAsResource(filename);
		} catch (Exception ex) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}


	@PostMapping("/audios/upload")
	public UploadResponse handleFileUpload(@RequestParam("file") MultipartFile file,
										   @RequestParam("source") String source,
										   @RequestParam(value = "source_id", required = false) Long sourceId,
										   @RequestParam(value = "source_author", required = false) String sourceAuthor,
										   @AuthenticationPrincipal CustomUserDetails userDetails) {

		int status = 1;

		AudioFile audioFile;

		User user = usersRepository.findByUsername(userDetails.getUsername());

		if (checkFormat(file.getOriginalFilename()) && audioRepository.findBydUserIdAndFileName(user.getUserId(), file.getOriginalFilename()) == null) {
			try {
				storageService.store(file);

				File storedFile = getStoredFile(file.getOriginalFilename(), user.getUsername());

				audioFile = new AudioFile(user.getUserId(), user.getUsername() + "/" + file.getOriginalFilename(),
							file.getOriginalFilename(), getDuration(storedFile), source, sourceId, sourceAuthor);

				audioRepository.save(audioFile);

				return new UploadResponse(file.getOriginalFilename(),
						"84.201.143.25:8080/audios/" + file.getOriginalFilename(),
							file.getSize(), status, audioFile.getRecordId());

			} catch (Exception exception) {
				System.err.println(exception.getMessage());

				File storedFile = getStoredFile(file.getOriginalFilename(), user.getUsername());
				if (storedFile.exists()) {
					storedFile.delete();
				}

				status = 0;
			}
		}
		else {
			status = -1;
		}

		return new UploadResponse(file.getOriginalFilename(), "",
					file.getSize(), status, (long) 0);
	}

	@PostMapping("/signup")
	public RegistrationResponse processRegister(@RequestBody User user) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);

		if (usersRepository.findByUsername(user.getUsername()) != null) {
			return new RegistrationResponse(user.getUsername(), "User already exist!", (long) -1);
		}

		Long userId = (long) -1;

		try {
			usersRepository.save(user);
			userId = usersRepository.findByUsername(user.getUsername()).getUserId();

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return new RegistrationResponse(user.getUsername(), "Registration error!", (long) -1);
		}

		return new RegistrationResponse(user.getUsername(), "User registered!", user.getUserId());
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
	public ResponseEntity<?> deleteAudio(@RequestParam("id") Long id,
										 @AuthenticationPrincipal CustomUserDetails userDetails) {
		try {

			User user = usersRepository.findByUsername(userDetails.getUsername());

			AudioFile audioFile = audioRepository.findByUserIDAAndRecordId(user.getUserId(), id);

			if (audioFile == null) {
				throw new Exception("Record doesn't exist!");
			}

			deleteAudioFile(audioFile.getRecordName(), user.getUsername());
			audioRepository.deleteById(id, user.getUserId());

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

	public File getStoredFile(String fileName, String username) {
		Path rootLocation = ((AudioStorageService)storageService).getRootLocation();
		Path userDirectory = rootLocation.resolve(Paths.get(username));
		Path storedFile = userDirectory.resolve(Paths.get(fileName))
				.normalize().toAbsolutePath();

		return storedFile.toFile();
	}

	public Time getDuration(File file) throws IOException, TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException {

		org.jaudiotagger.audio.AudioFile audioFile = AudioFileIO.read(file);
		AudioHeader audioHeader = audioFile.getAudioHeader();

		int seconds = audioHeader.getTrackLength();
		int minutes = seconds / 60;
		seconds %= 60;
		int hours = minutes / 60;
		minutes %= 60;

		return new Time(hours, minutes, seconds);
	}

	public void deleteAudioFile(String fileName, String username) {
		Path rootLocation = ((AudioStorageService)storageService).getRootLocation();
		Path userDirectory = rootLocation.resolve(Paths.get(username));
		Path storedFilePath = userDirectory.resolve(Paths.get(fileName))
				.normalize().toAbsolutePath();

		File storedFile = storedFilePath.toFile();
		if (storedFile.exists()) {
			storedFile.delete();
		}
	}

}
