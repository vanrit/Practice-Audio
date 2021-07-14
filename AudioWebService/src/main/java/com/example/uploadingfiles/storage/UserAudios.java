package com.example.uploadingfiles.storage;

import com.example.uploadingfiles.user.AudioFile;

import java.util.List;

public class UserAudios {
    private List<AudioFile> userAudios;

    public List<AudioFile> getUserAudios() {
        return userAudios;
    }

    public void setUserAudios(List<AudioFile> userAudios) {
        this.userAudios = userAudios;
    }
}
