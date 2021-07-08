package com.example.uploadingfiles.user;

import org.springframework.data.annotation.Reference;

import javax.persistence.*;

@Entity
@Table(name = "audios")
public class AudioFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long audioId;

    @Column
    private String username;

    @Column
    private String fileName;

    public Long getAudioId() {
        return audioId;
    }

    public void setAudioId(Long audioID) {
        this.audioId = audioID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
