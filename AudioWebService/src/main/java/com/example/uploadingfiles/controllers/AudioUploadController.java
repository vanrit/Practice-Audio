package com.example.uploadingfiles.controllers;

import com.example.uploadingfiles.repository.AudioRepository;
import com.example.uploadingfiles.responses.UploadResponse;
import com.example.uploadingfiles.repository.UsersRepository;
import com.example.uploadingfiles.service.AudioStorageService;
import com.example.uploadingfiles.user.AudioFile;
import com.example.uploadingfiles.user.CustomUserDetails;
import com.example.uploadingfiles.user.User;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.uploadingfiles.exceptions.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class AudioUploadController {

	private static final Set<String> formats = new HashSet<>(Arrays.asList("ogg", "mp3", "flac", "mp4",
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

	/**
	 * Возвращает методы для работы с веб-сервисом.
	 * @return
	 */
	@GetMapping("/")
	@ResponseBody
	public String getMethods() {

		String answer = "Доступные методы:<br>" +
				"1) 178.154.192.134:8080/audios/all<br>" +
				"* Тип запроса: GET<br>" +
				"* Ответ: JSON, содержащий список всех аудиофайлов, с описанием их полей(record_id, user_id, path, record_name, duration, source, source_id, source_author)<br><br>" +
				"2) 178.154.192.134:8080/audios/*filename*<br>" +
				"* Тип запроса: GET<br>" +
				"* Ответ: аудиофайл<br><br>" +
				"3) 178.154.192.134:8080/audios/upload<br>" +
				"* Тип запроса: POST<br>" +
				"* Тело запроса:<br>" +
				"	1. Поле file, содержащее аудиофайл<br>" +
				"	2. Поле source, содержащее информацию об источнике аудиозаписи (Local, Telegram, Whatsapp)<br>" +
				"	3. (Опционально) Поле scope, определяющая уровень доступа к аудиозаписи (private[по умолчанию] или public)<br>" +
				"	4. (Опционально) Поле source_author, содержащее имя автора аудиозаписи<br>" +
				"	5. (Опционально) Поле source_id, содержащее id автора, если он является зарегистрированным пользователем<br>" +
				"* Ответ: JSON, содержащий информацию о результате загрузки (fileName, songId, fileDownloadUri, size, status (1 - загрузка выполнена успешно, 0 - ошибка во время сохранения файла, -1 - некорректный формат файла/файл с таким именем уже загружен))<br>" +
				"* Поддерживаемые форматы: ogg, mp3, flac, mp4, m4a, m4p, wma, wav, ra, rm, m4b, ai<br><br>" +
				"4) 178.154.192.134:8080/signup<br>" +
				"* Тип запроса: POST<br>" +
				"* Тело запроса: JSON, содержащий поля:<br>" +
				"	1. firstName - имя пользователя<br>" +
				"	2. lastName - фамилия пользователя<br>" +
				"	3. username - логин пользователя<br>" +
				"	4. password - пароль пользователя<br>" +
				"* Ответ: JSON, содержащий поля:<br>" +
				"	1. username - имя пользователя<br>" +
				"	2. id - id пользователя (0 - в случае неудачной регистрации)<br>" +
				"	3. status - статус регистрации:<br>" +
				"		- User registered! - пользователь успешно зарегистрирован!<br>" +
				"		- User already exist! - пользователь с указанным логином уже существует<br>" +
				"		- Registration error! - ошибка во время регистрации<br><br>" +
				"5) 178.154.192.134:8080/login<br>" +
				"* Тип запроса: POST<br>" +
				"* Тело запроса:<br>" +
				"	1. Поле username - логин пользователя<br>" +
				"	2. Поле password - пароль пользователя<br>" +
				"* Ответ: Код 200 в случае успешной регистрации, 401 - в случае ошибочных данных<br><br>" +
				"6) 178.154.192.134:8080/audios/delete<br>" +
				"* Тип запроса: DELETE<br>" +
				"* Тело запроса: Поле id, содержащее id аудиофайла для удаления<br>" +
				"* Ответ: Код 200, в случае успешного удаления, 404 - файл с указанным id отсутствует на сервере<br><br>" +
				"7) 178.154.192.134:8080/logout<br>" +
				"* Тип запроса: Любой<br>" +
				"* Ответ: Код 200, в случае успешного выхода из аккаунта<br><br>" +
				"8) 178.154.192.134:8080/audios/public/all<br>" +
				"* Тип запроса: GET<br>" +
				"* Ответ: JSON, содержащий информацию о всех публичный аудиозаписях (record_id, user_id, path, record_name, duration, source, source_id, source_author, scope)<br><br>" +
				"9) 178.154.192.134:8080/audios/public/*filename*<br>" +
				"* Тип запроса: GET<br>" +
				"* Ответ: аудиофайл с публичным доступом<br><br>" +
				"10) 178.154.192.134:8080/audios/recognition<br>" +
				"* Тип запроса: POST<br>" +
				"* Тело запроса: поле file, содержащее аудиозапись<br>" +
				"* Ответ: JSON, содержащий информацию о распонанной аудиозаписи (record_id, user_id, path, record_name, duration, source, source_id, source_author, scope)<br><br>" +
				"Методы для администратора:<br>" +
				"1) 178.154.192.134:8080/admin/users<br>" +
				"* Тип запроса: GET<br>" +
				"* Ответ: JSON, содержащий информацию о всех пользователях (firstName, lastName, username, password)<br><br>" +
				"2) 178.154.192.134:8080/admin/audios<br>" +
				"* Тип запроса: GET<br>" +
				"* Ответ: JSON, содержащий информацию о всех аудиозаписях пользователей (record_id, user_id, path, record_name, duration, source, source_id, source_author, scope)<br><br>" +
				"3) 178.154.192.134:8080/admin/delete_audio<br>" +
				"* Тип запроса: DELETE<br>" +
				"* Тело запроса: поле recordId, содержащее id записи, которую надо удалить<br><br>" +
				"4) 178.154.192.134:8080/admin/delete_user<br>" +
				"* Тип запроса: DELETE<br>" +
				"* Тело запроса: поле userId, содержащее id пользователя, которого надо удалить<br><br>";


		return answer;
	}

	/**
	 * Возвращает все загруженные пользователем аудиозаписи.
	 * @param userDetails
	 * @return
	 */
	@GetMapping("/audios/all")
	@ResponseBody
	public List<AudioFile> getUserRecords(@AuthenticationPrincipal CustomUserDetails userDetails) {

		User user = usersRepository.findByUsername(userDetails.getUsername());

		return audioRepository.findAllByUserId(user.getUserId());

	}

	/**
	 * Возвращает все публичные аудиозаписи.
	 * @return
	 */
	@GetMapping("/audios/public/all")
	@ResponseBody
	public List<AudioFile> getPublicRecords() {
		return audioRepository.findByScope("public");
	}

	/**
	 * Возвращает файл с аудиозаписью *filename*
	 * @param filename
	 * @return
	 */
	@GetMapping("/audios/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveRecord(@PathVariable String filename) {

		String username = ((CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
		User user = usersRepository.findByUsername(username);

		if (audioRepository.findBydUserIdAndRecordName(user.getUserId(), filename) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		Resource file;

		try {
			file = storageService.loadAsResource(filename, "private");
		} catch (Exception ex) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	/**
	 * Возвращает публичный файл с именем *filename*
	 * @param filename
	 * @return
	 */
	@GetMapping("/audios/public/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> servePublicRecord(@PathVariable String filename) {

		if (audioRepository.findByRecordNameAndScope(filename, "public") == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		Resource file;

		try {
			file = storageService.loadAsResource(filename, "public");
		} catch (Exception ex) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}


	/**
	 * Загрузка аудиозаписи на сервер.
	 * @param file - файл с аудиозаписью
	 * @param source - источник аудиозаписи (Local, Telegram или Whatsapp)
	 * @param scope - доступ к аудиозаписи (public или private)
	 * @param sourceId - id пользователя-источника аудиозаписи
	 * @param sourceAuthor - имя автора аудизаписи
	 * @param userDetails
	 * @return
	 */
	@PostMapping("/audios/upload")
	public UploadResponse handleFileUpload(@RequestParam("file") MultipartFile file,
										   @RequestParam("source") String source,
										   @RequestParam(value = "scope", required = false, defaultValue = "private") String scope,
										   @RequestParam(value = "source_id", required = false) Long sourceId,
										   @RequestParam(value = "source_author", required = false) String sourceAuthor,
										   @AuthenticationPrincipal CustomUserDetails userDetails) {

		int status = 1;

		AudioFile audioFile;

		User user = usersRepository.findByUsername(userDetails.getUsername());

		if (checkFormat(file.getOriginalFilename()) &&
				((scope.equals("private") && audioRepository.findBydUserIdAndRecordName(user.getUserId(), file.getOriginalFilename()) == null) ||
				(scope.equals("public") && audioRepository.findByRecordNameAndScope(file.getOriginalFilename(), "public") == null))) {
			try {
				storageService.store(file, scope);

				File storedFile = getStoredFile(file.getOriginalFilename(), user.getUsername(), scope);

				audioFile = new AudioFile(user.getUserId(), (scope.equals("public") ? "public" : user.getUsername()) + "/" + file.getOriginalFilename(),
							file.getOriginalFilename(), getDuration(storedFile), source, sourceId, sourceAuthor, scope);

				audioRepository.save(audioFile);

				return new UploadResponse(file.getOriginalFilename(),
						"178.154.192.134:8080/audios/" + (scope.equals("public") ? "public/" : "") + file.getOriginalFilename(),
							file.getSize(), status, audioFile.getRecordId());

			} catch (Exception exception) {
				System.err.println(exception.getMessage());

				File storedFile = getStoredFile(file.getOriginalFilename(), user.getUsername(), scope);
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

	/**
	 * Распознавание переданной аудиозаписи.
	 * @param file
	 * @return Распознанная аудиозапись.
	 */
	@PostMapping("/audios/recognition")
	public AudioFile audioRecognition(@RequestParam("file") MultipartFile file) {
		return recogniseFromFile(file);
	}

	/**
	 * Удаление аудиозаписи по id.
	 * @param id
	 * @param userDetails
	 * @return
	 */
	@DeleteMapping("/audios/delete")
	public ResponseEntity<?> deleteAudio(@RequestParam("id") Long id,
										 @AuthenticationPrincipal CustomUserDetails userDetails) {
		try {

			User user = usersRepository.findByUsername(userDetails.getUsername());

			AudioFile audioFile = audioRepository.findByUserIDAAndRecordId(user.getUserId(), id);

			if (audioFile == null) {
				throw new Exception("Record doesn't exist!");
			}

			String scope = audioFile.getScope();
			deleteAudioFile(audioFile.getRecordName(), user.getUsername(), scope);
			audioRepository.deleteRecordById(id, user.getUserId());

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Обработка StorageFileNotFoundException
	 * @param exc
	 * @return
	 */
	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

	/**
	 * Проверка формата файла по его названию.
	 * @param fileName
	 * @return
	 */
	public boolean checkFormat(String fileName) {
		String fileFormat = fileName.substring(fileName.indexOf('.') + 1);

		return formats.stream().anyMatch(a -> a.equals(fileFormat));
	}

	/**
	 * Получение хранящегося на сервере файла по его имени, имени пользователя
	 * и уровне доступа.
	 * @param fileName
	 * @param username
	 * @param scope
	 * @return
	 */
	public File getStoredFile(String fileName, String username, String scope) {
		Path rootLocation = ((AudioStorageService)storageService).getRootLocation();

		Path directory;

		if (scope.equals("public")) {
			directory = rootLocation.resolve(Paths.get("public"));
		}
		else {
			directory = rootLocation.resolve(Paths.get(username));
		}

		Path storedFile = directory.resolve(Paths.get(fileName))
				.normalize().toAbsolutePath();

		return storedFile.toFile();
	}

	/**
	 * Получение значения длительности аудиозаписи из файла.
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws TagException
	 * @throws ReadOnlyFileException
	 * @throws CannotReadException
	 * @throws InvalidAudioFrameException
	 */
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

	/**
	 * Удаление файла по его имени, имени пользователя и уровню доступа.
	 * @param fileName
	 * @param username
	 * @param scope
	 */
	public void deleteAudioFile(String fileName, String username, String scope) {
		Path rootLocation = ((AudioStorageService)storageService).getRootLocation();

		Path storeDirectory = rootLocation.resolve(Paths.get(username));
		if (scope.equals("public")) {
			storeDirectory = rootLocation.resolve(Paths.get("public"));
		}

		Path storedFilePath = storeDirectory.resolve(Paths.get(fileName))
				.normalize().toAbsolutePath();

		File storedFile = storedFilePath.toFile();
		if (storedFile.exists()) {
			storedFile.delete();
		}
	}

	/**
	 * Распознавание аудиозаписи по переданному файлу (заглушка).
	 * @param file
	 * @return
	 */
	public AudioFile recogniseFromFile(MultipartFile file) {

		AudioFile audioFile = new AudioFile();
		audioFile.setRecordId((long)1);
		audioFile.setUserId((long)1);
		audioFile.setSourceAuthor("Queen");
		audioFile.setSourceId((long)1);
		audioFile.setSource("Local");
		audioFile.setPath("user/Bohemian_Rhapsody.mp3");
		audioFile.setScope("public");
		audioFile.setRecordName("Bohemian_Rhapsody.mp3");
		audioFile.setDuration(new Time(0, 5, 54));

		return audioFile;
	}

}
