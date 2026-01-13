package com.example.backend.service;

import com.example.backend.model.Event;
import com.example.backend.utils.BackendPaths;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.time.LocalDateTime;

/**
 * EventCsvService manages event data persistence for the backend using CSV file
 * storage.
 * 
 * This service provides CRUD operations for calendar events:
 * - Creating new events with auto-generated IDs
 * - Reading/loading all events
 * - Updating existing events
 * - Deleting events (individual or by user ID)
 * - Managing recurring events
 * 
 * File location: backend/csvFiles/events.csv
 * CSV format: id,userId,title,description,startDateTime,endDateTime,category
 * 
 * All file operations use UTF-8 encoding.
 * Thread-safe ID generation.
 */
public class EventCsvService {

    /** Path to the events CSV file */
    private final Path csvPath = BackendPaths.resolveBackendDir().resolve("csvFiles").resolve("events.csv");

    /**
     * Constructs an EventCsvService and initializes the CSV file.
     */
    public EventCsvService() {
        initializeCsv();
    }

    /**
     * Initializes the events CSV file if it doesn't exist.
     * 
     * Creates parent directory and writes CSV header with UTF-8 encoding.
     * Header: "id,userId,title,description,startDateTime,endDateTime,category"
     */
    private void initializeCsv() {
        File file = csvPath.toFile();
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        if (!file.exists()) {
            try (Writer fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                fw.write("id,userId,title,description,startDateTime,endDateTime,category\n");
            } catch (IOException e) {
                System.out.println("Error initializing events CSV: " + e.getMessage());
            }
        }
    }

    /**
     * Loads all events from the CSV file.
     * 
     * Reads using UTF-8 encoding and parses each row into Event objects.
     * Skips invalid rows (< 6 fields, invalid dates, or parsing errors).
     * Defaults category to "PERSONAL" if not specified.
     * 
     * @return List of all events (empty if file doesn't exist or error occurs)
     */
    public List<Event> loadEvents() {
        List<Event> events = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(csvPath.toFile()), StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",", -1);

                if (fields.length < 6)
                    continue;

                Event event = new Event();
                try {
                    event.setId(Integer.parseInt(fields[0]));
                    event.setUserId(Integer.parseInt(fields[1]));
                } catch (NumberFormatException e) {
                    continue;
                }
                event.setTitle(fields[2]);
                event.setDescription(fields[3]);
                try {
                    event.setStartDateTime(LocalDateTime.parse(fields[4]));
                    event.setEndDateTime(LocalDateTime.parse(fields[5]));
                } catch (Exception e) {
                    continue;
                }

                if (fields.length > 6 && !fields[6].isEmpty()) {
                    event.setCategory(fields[6].trim());
                } else {
                    event.setCategory("PERSONAL");
                }

                events.add(event);
            }
        } catch (Exception e) {
            System.out.println("Error reading CSV: " + e.getMessage());
        }

        return events;
    }

    /**
     * Generates the next available event ID.
     * 
     * Scans all events to find the maximum ID and returns max + 1.
     * Thread-safe to prevent ID conflicts.
     * 
     * @return Next unique event ID (1 if no events exist)
     */
    public synchronized int getNextId() {
        int maxId = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(csvPath.toFile()), StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length > 0 && !fields[0].isEmpty()) {
                    try {
                        int id = Integer.parseInt(fields[0]);
                        if (id > maxId)
                            maxId = id;
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
        } catch (IOException e) {
            return 1;
        }
        return maxId + 1;
    }

    /**
     * Saves a new event to the CSV file.
     * 
     * Auto-generates and assigns a new ID to the event.
     * Ensures proper CSV formatting and UTF-8 encoding.
     * Defaults category to "PERSONAL" if not set.
     * 
     * @param event The event to save (ID will be auto-assigned)
     */
    public void saveEvent(Event event) {
        int newId = getNextId();
        event.setId(newId);

        ensureNewline();

        try (Writer fw = new OutputStreamWriter(new FileOutputStream(csvPath.toFile(), true), StandardCharsets.UTF_8)) {
            String category = event.getCategory() != null ? event.getCategory() : "PERSONAL";
            fw.write(
                    event.getId() + "," +
                            event.getUserId() + "," +
                            event.getTitle() + "," +
                            event.getDescription() + "," +
                            (event.getStartDateTime() != null ? event.getStartDateTime().toString() : "") + "," +
                            (event.getEndDateTime() != null ? event.getEndDateTime().toString() : "") + "," +
                            category + "\n");
        } catch (Exception e) {
            System.out.println("Error writing to CSV: " + e.getMessage());
        }
    }

    /**
     * Saves a recurring event (currently just saves the single event).
     * 
     * Note: Recurring event generation logic is handled in the frontend.
     * This method simply delegates to saveEvent().
     * 
     * @param event The base event for recurrence
     */
    public void generateAndSaveRecurringEvents(Event event) {
        saveEvent(event);
    }

    /**
     * Ensures the CSV file ends with a newline character.
     * 
     * Prevents malformed CSV by checking the last byte and appending
     * a newline if necessary.
     */
    private void ensureNewline() {
        File file = csvPath.toFile();
        if (!file.exists() || file.length() == 0)
            return;

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(file.length() - 1);
            if (raf.read() != '\n') {
                try (Writer fw = new OutputStreamWriter(new FileOutputStream(csvPath.toFile(), true),
                        StandardCharsets.UTF_8)) {
                    fw.write("\n");
                } catch (IOException e) {
                }
            }
        } catch (IOException e) {
        }
    }

    /**
     * Updates an existing event.
     * 
     * Loads all events, finds the matching event by ID, replaces it,
     * and rewrites the entire CSV file.
     * 
     * @param id           The ID of the event to update
     * @param updatedEvent The event with updated information
     */
    public void updateEvent(int id, Event updatedEvent) {
        List<Event> allEvents = loadEvents();
        boolean found = false;
        for (int i = 0; i < allEvents.size(); i++) {
            if (allEvents.get(i).getId() == id) {
                updatedEvent.setId(id);
                allEvents.set(i, updatedEvent);
                found = true;
                break;
            }
        }

        if (found) {
            rewriteCsv(allEvents);
        }
    }

    /**
     * Deletes an event by ID.
     * 
     * Removes the event from the loaded events and rewrites the CSV file.
     * 
     * @param id The ID of the event to delete
     */
    public void deleteEvent(int id) {
        List<Event> allEvents = loadEvents();
        boolean removed = allEvents.removeIf(e -> e.getId() == id);

        if (removed) {
            rewriteCsv(allEvents);
        }
    }

    /**
     * Deletes all events belonging to a specific user.
     * 
     * Useful for account deletion or data cleanup.
     * 
     * @param userId The ID of the user whose events should be deleted
     */
    public void deleteEventsByUserId(int userId) {
        List<Event> allEvents = loadEvents();
        boolean removed = allEvents.removeIf(e -> e.getUserId() == userId);

        if (removed) {
            rewriteCsv(allEvents);
        }
    }

    /**
     * Retrieves all event IDs belonging to a specific user.
     * 
     * @param userId The user ID to filter by
     * @return List of event IDs (empty if user has no events)
     */
    public List<Integer> getEventIdsByUserId(int userId) {
        List<Event> allEvents = loadEvents();
        List<Integer> eventIds = new ArrayList<>();

        for (Event event : allEvents) {
            if (event.getUserId() == userId) {
                eventIds.add(event.getId());
            }
        }

        return eventIds;
    }

    /**
     * Rewrites the entire CSV file with the provided list of events.
     * 
     * Used after update/delete operations to persist changes.
     * Writes header and all events with UTF-8 encoding.
     * 
     * @param events The complete list of events to write
     */
    private void rewriteCsv(List<Event> events) {
        try (Writer fw = new OutputStreamWriter(new FileOutputStream(csvPath.toFile(), false),
                StandardCharsets.UTF_8)) {
            fw.write("id,userId,title,description,startDateTime,endDateTime,category\n");
            for (Event event : events) {
                String category = event.getCategory() != null ? event.getCategory() : "PERSONAL";
                fw.write(
                        event.getId() + "," +
                                event.getUserId() + "," +
                                event.getTitle() + "," +
                                event.getDescription() + "," +
                                (event.getStartDateTime() != null ? event.getStartDateTime().toString() : "") + "," +
                                (event.getEndDateTime() != null ? event.getEndDateTime().toString() : "") + "," +
                                category + "\n");
            }
        } catch (Exception e) {
            System.out.println("Error rewriting CSV: " + e.getMessage());
        }
    }
}
