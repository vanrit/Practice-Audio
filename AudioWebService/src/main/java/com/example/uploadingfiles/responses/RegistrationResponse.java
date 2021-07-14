package com.example.uploadingfiles.responses;

public class RegistrationResponse {
    private String username;
    private Long id;
    private String status;

    public RegistrationResponse(String username, String status, Long id) {
        this.username = username;
        this.status = status;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
