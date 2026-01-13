package com.example.backend.model;

import java.time.LocalDateTime;

/**
 * Event represents a calendar event in the backend system.
 * 
 * Similar to the frontend Event model but with additional constructor overloads
 * for flexible event creation from different data sources (JSON, CSV, etc.).
 * 
 * Features:
 * - Basic event properties: id, userId, title, description, dates, category
 * - Recurrence support: interval, times, end date
 * - Multiple constructors for different use cases
 * - Date parsing utility for flexible date string formats
 */
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

    /**
     * Default constructor for empty Event creation.
     */
    public Event() {
    }

    /**
     * Full constructor with LocalDateTime objects.
     * 
     * @param id            Event ID
     * @param userId        User who owns this event
     * @param title         Event title
     * @param description   Event description
     * @param startDateTime Start date and time
     * @param endDateTime   End date and time
     * @param category      Event category (e.g., "WORK", "PERSONAL")
     */
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

    /**
     * Constructor without ID and userId (for creating new events).
     * 
     * @param title         Event title
     * @param description   Event description
     * @param startDateTime Start date and time
     * @param endDateTime   End date and time
     * @param category      Event category
     */
    public Event(String title, String description, LocalDateTime startDateTime, LocalDateTime endDateTime,
            String category) {
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.category = category;
    }

    /**
     * Constructor with string dates for parsing flexibility.
     * Useful for CSV import or form data where dates arrive as strings.
     * 
     * @param id          Event ID
     * @param userId      User who owns this event
     * @param title       Event title
     * @param description Event description
     * @param startStr    Start date as string (ISO format or "YYYY-MM-DD")
     * @param endStr      End date as string (ISO format or "YYYY-MM-DD")
     * @param category    Event category
     */
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

    /**
     * Parses a date string to LocalDateTime.
     * 
     * Supports two formats:
     * - ISO 8601 with time: "2026-01-15T14:30:00"
     * - Date only: "2026-01-15" (assumes midnight 00:00:00)
     * 
     * @param d Date string to parse, or null
     * @return LocalDateTime object, or null if input is null
     */
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
