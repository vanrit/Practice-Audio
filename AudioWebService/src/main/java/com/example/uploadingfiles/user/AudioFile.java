package com.example.uploadingfiles.user;

import org.springframework.context.annotation.Primary;
import org.springframework.data.annotation.Reference;

import javax.persistence.*;
import java.sql.Time;

@Entity
@Table(name = "records")
public class AudioFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "user_id")
    private Long userId;

    @Column
    private String path;

    @Column
    private String scope;

    @Column(name = "record_name")
    private String recordName;

    @Column
    private Time duration;

    @Column
    private String source;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "source_author")
    private String sourceAuthor;

    public AudioFile(Long userId, String path, String recordName, Time duration, String source, Long sourceId, String sourceAuthor, String scope) {
        this.userId = userId;
        this.path = path;
        this.recordName = recordName;
        this.duration = duration;
        this.source = source;
        this.sourceId = sourceId;
        this.sourceAuthor = sourceAuthor;
        this.scope = scope;
    }

    public AudioFile(){}

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long record) {
        this.recordId = record;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    public Time getDuration() {
        return duration;
    }

    public void setDuration(Time duration) {
        this.duration = duration;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceAuthor() {
        return sourceAuthor;
    }

    public void setSourceAuthor(String sourceAuthor) {
        this.sourceAuthor = sourceAuthor;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
