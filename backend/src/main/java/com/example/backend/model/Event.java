package com.example.backend.model;

import java.time.LocalDateTime;

public class Event {
    private int id;
    private int userId;
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String category;

    private String recurrentInterval;
    private String recurrentTimes;
    private String recurrentEndDate;

    public Event() {
    }

    public Event(int id, int userId, String title, String description, LocalDateTime startDateTime,
            LocalDateTime endDateTime, String category) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.category = category;
    }

    public Event(String title, String description, LocalDateTime startDateTime, LocalDateTime endDateTime,
            String category) {
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.category = category;
    }

    public Event(int id, int userId, String title, String description, String startStr, String endStr,
            String category) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.startDateTime = parseDate(startStr);
        this.endDateTime = parseDate(endStr);
        this.category = category;
    }

    private LocalDateTime parseDate(String d) {
        if (d == null)
            return null;
        if (d.contains("T"))
            return LocalDateTime.parse(d);
        return LocalDateTime.parse(d + "T00:00:00");
    }

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
}
