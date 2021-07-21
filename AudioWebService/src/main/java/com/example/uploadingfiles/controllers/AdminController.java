package com.example.uploadingfiles.controllers;

import com.example.uploadingfiles.repository.AudioRepository;
import com.example.uploadingfiles.repository.UsersRepository;
import com.example.uploadingfiles.service.AudioStorageService;
import com.example.uploadingfiles.storage.StorageService;
import com.example.uploadingfiles.user.AudioFile;
import com.example.uploadingfiles.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class AdminController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AudioRepository audioRepository;

    @Autowired
    private StorageService storageService;

    /**
     * Получение данных обо всех пользователях.
     * @return
     */
    @GetMapping("/admin/users")
    @ResponseBody
    public List<User> getUsers() {
        return usersRepository.findAll();
    }

    /**
     * Получение данных обо всех аудиозаписях.
     * @return
     */
    @GetMapping("/admin/audios")
    @ResponseBody
    public List<AudioFile> getAudios() {
        return audioRepository.findAll();
    }

    /**
     * Удаление пользователя по id.
     * @param userId
     * @return
     */
    @DeleteMapping("/admin/delete_user")
    public ResponseEntity<?> deleteUser(@RequestParam Long userId) {

        if (usersRepository.findById(userId).get().getUsername().equals("admin")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {

            String username = usersRepository.findById(userId).get().getUsername();

            audioRepository.deleteAllByUserId(userId);
            usersRepository.deleteById(userId);
            deleteUserDirectory(username);

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception exception) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Удаление аудиозаписи по id.
     * @param recordId
     * @return
     */
    @DeleteMapping("/admin/delete_audio")
    public ResponseEntity<?> deleteAudio(@RequestParam Long recordId) {

        try {
            AudioFile audioFile = audioRepository.findById(recordId).get();

            Long userId = audioFile.getUserId();
            String recordName = audioFile.getRecordName();
            String scope = audioFile.getScope();
            String username = usersRepository.findById(userId).get().getUsername();

            audioRepository.deleteById(recordId);
            deleteAudioFile(recordName, username, scope);

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception exception) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Удаление директории пользователя.
     * @param username
     */
    public void deleteUserDirectory(String username) {
        if (storageService instanceof AudioStorageService) {
            Path rootLocation = ((AudioStorageService) storageService).getRootLocation();
            File userDirectory = rootLocation.resolve(Paths.get(username)).toFile();

            if (userDirectory.exists()) {

                String[]files = userDirectory.list();
                for (String fileName: files){
                    File currentFile = new File(userDirectory.getPath(), fileName);
                    currentFile.delete();
                }

                userDirectory.delete();
            }
        }
    }

    /**
     * Удаление фалйла пользователя.
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
}
