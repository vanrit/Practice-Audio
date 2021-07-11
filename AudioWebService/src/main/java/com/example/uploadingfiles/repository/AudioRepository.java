package com.example.uploadingfiles.repository;

import com.example.uploadingfiles.user.AudioFile;
import com.example.uploadingfiles.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AudioRepository extends JpaRepository<AudioFile, Long> {
    @Query("SELECT audioFile FROM AudioFile audioFile WHERE audioFile.username = ?1 and audioFile.fileName = ?2")
    public AudioFile findByFileNameAndUsername(String username, String fileName);

    @Query("DELETE FROM AudioFile audioFile where audioFile.audioId = ?1")
    @Modifying
    void deleteById(Long id);
}
