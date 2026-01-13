package com.example.frontend.model;

import java.time.LocalDateTime;

/**
 * CalendarEvent is a simplified event model used specifically for calendar
 * display.
 * 
 * Unlike the full Event model, CalendarEvent focuses on the minimal data needed
 * to render events on a calendar view:
 * - Event identification and timing
 * - Visual representation (color)
 * - Category association
 * 
 * This is typically used as a view model, separating display concerns from
 * the full business logic Event model.
 */
public class CalendarEvent {
    private String id;
    private String title;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String color; // Stores Hex Color
    private String category; // Stores Category ID

    /**
     * Constructs a CalendarEvent with all properties.
     * 
     * @param id            Unique identifier for the event
     * @param title         Event title to display
     * @param startDateTime When the event starts
     * @param endDateTime   When the event ends
     * @param color         Hexadecimal color code for visual representation (e.g.,
     *                      "#FF5733")
     * @param category      Category ID identifying the event type
     */
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
