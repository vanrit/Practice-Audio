package com.example.uploadingfiles.responses;

public class UploadResponse {
    private Long songId;
    private String fileName;
    private String fileDownloadUri;
    private long size;
    private int status;

    public UploadResponse(String fileName, String fileDownloadUri, long size, int status, Long songId) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.size = size;
        this.status = status;
        this.songId = songId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileDownloadUri() {
        return fileDownloadUri;
    }

    public void setFileDownloadUri(String fileDownloadUri) {
        this.fileDownloadUri = fileDownloadUri;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getSongId() {
        return songId;
    }

    public void setSongId(Long songId) {
        this.songId = songId;
    }
}
