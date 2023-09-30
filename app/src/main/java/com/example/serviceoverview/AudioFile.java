package com.example.serviceoverview;

import androidx.annotation.NonNull;

public class AudioFile {
    private String title;
    private String displayName;
    private long musicId;
    private int durationInMS;
    private String artist;

    public AudioFile() {
    }

    public AudioFile(String title, String displayName, long id, int durationInMS, String artist) {
        this.title = title;
        this.displayName = displayName;
        this.musicId = id;
        this.durationInMS = durationInMS;
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getMusicId() {
        return musicId;
    }

    public void setMusicId(long musicId) {
        this.musicId = musicId;
    }

    public int getDurationInMS() {
        return durationInMS;
    }

    public void setDurationInMS(int durationInMS) {
        this.durationInMS = durationInMS;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @NonNull
    @Override
    public String toString() {
        return "AudioFile{" +
                "title='" + title + '\'' +
                ", displayName='" + displayName + '\'' +
                ", id=" + musicId +
                ", durationInMS=" + durationInMS +
                ", artist='" + artist + '\'' +
                '}';
    }
}
