package com.example.frontend.model;

import java.time.LocalDateTime;

public class Event {
    private int id;
    private int userId;
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private String recurrentInterval;
    private String recurrentTimes;
    private String recurrentEndDate;

    private String category;

    public Event() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getRecurrentInterval() {
        return recurrentInterval;
    }

    public void setRecurrentInterval(String recurrentInterval) {
        this.recurrentInterval = recurrentInterval;
    }

    public String getRecurrentTimes() {
        return recurrentTimes;
    }

    public void setRecurrentTimes(String recurrentTimes) {
        this.recurrentTimes = recurrentTimes;
    }

    public String getRecurrentEndDate() {
        return recurrentEndDate;
    }

    public void setRecurrentEndDate(String recurrentEndDate) {
        this.recurrentEndDate = recurrentEndDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
