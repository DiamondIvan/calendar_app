package com.example.frontend.model;

import java.time.LocalDateTime;

public class CalendarEvent {
    private String id;
    private String title;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String color; // Stores Hex Color
    private String category; // Stores Category ID

    public CalendarEvent(String id, String title, LocalDateTime startDateTime, LocalDateTime endDateTime, String color,
            String category) {
        this.id = id;
        this.title = title;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.color = color;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public String getColor() {
        return color;
    }

    public String getCategory() {
        return category;
    }
}
