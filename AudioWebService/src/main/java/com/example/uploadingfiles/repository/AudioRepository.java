package com.example.uploadingfiles.repository;

import com.example.uploadingfiles.user.AudioFile;
import com.example.uploadingfiles.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AudioRepository extends JpaRepository<AudioFile, Long> {
    @Query("SELECT audioFile FROM AudioFile audioFile WHERE audioFile.userId = ?1 and audioFile.recordName = ?2")
    AudioFile findBydUserIdAndFileName(Long userId, String fileName);

    @Query("SELECT audioFile FROM AudioFile audioFile WHERE audioFile.userId = ?1 and audioFile.recordId = ?2")
    AudioFile findByUserIDAAndRecordId(Long userId, Long recordId);

    @Query("DELETE FROM AudioFile audioFile where audioFile.recordId = ?1 and audioFile.userId = ?2")
    @Modifying
    @Transactional
    void deleteById(Long recordId, Long userId);

    @Query("SELECT audioFile FROM AudioFile audioFile WHERE audioFile.userId = ?1")
    List<AudioFile> findAllByUserId(Long userId);

}
