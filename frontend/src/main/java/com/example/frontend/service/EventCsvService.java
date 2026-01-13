package com.example.frontend.service;

import com.example.frontend.model.Event;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * EventCsvService manages event data persistence for the frontend using Jackson
 * CSV processing.
 * 
 * This advanced service provides:
 * - CRUD operations with Jackson ObjectMapper for type-safe CSV handling
 * - Recurring event generation (daily, weekly, monthly, yearly)
 * - Automatic validation and filtering of corrupted CSV entries
 * - Separate storage for event data and recurrence rules
 * - Thread-safe operations with synchronized methods
 * - Automatic CSV file path resolution and initialization
 * 
 * Files managed:
 * - events.csv: id,userId,title,description,startDateTime,endDateTime,category
 * - recurrent.csv: eventId,recurrentInterval,recurrentTimes,recurrentEndDate
 * 
 * Uses Jackson CsvMapper with JavaTimeModule for LocalDateTime support.
 * All operations filter out invalid/corrupted events automatically.
 */
public class EventCsvService {

    /** Path to the events CSV file */
    private String csvPath;

    /** Path to the recurrent events metadata CSV file */
    private String recurrentCsvPath;

    /** Jackson CSV mapper configured for Event objects */
    private final CsvMapper mapper;

    /** CSV schema for event data */
    private final CsvSchema eventSchema;

    /** CSV schema for recurrence metadata */
    private final CsvSchema recurrentSchema;

    /**
     * Constructs an EventCsvService with Jackson CSV processing.
     * 
     * Initialization steps:
     * 1. Creates and configures CsvMapper with JavaTimeModule
     * 2. Disables timestamp serialization for readable dates
     * 3. Configures to ignore unknown CSV columns
     * 4. Defines event schema (7 columns)
     * 5. Defines recurrence schema (4 columns)
     * 6. Resolves CSV file paths
     * 7. Initializes CSV files if they don't exist
     */
    public EventCsvService() {
        this.mapper = new CsvMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Basic Event Schema
        this.eventSchema = CsvSchema.builder()
                .addColumn("id", CsvSchema.ColumnType.NUMBER)
                .addColumn("userId", CsvSchema.ColumnType.NUMBER)
                .addColumn("title")
                .addColumn("description")
                .addColumn("startDateTime")
                .addColumn("endDateTime")
                .addColumn("category")
                .setUseHeader(true)
                .build();

        // Recurrent Event Schema
        this.recurrentSchema = CsvSchema.builder()
                .addColumn("eventId", CsvSchema.ColumnType.NUMBER)
                .addColumn("recurrentInterval")
                .addColumn("recurrentTimes")
                .addColumn("recurrentEndDate")
                .setUseHeader(true)
                .build();

        resolveCsvPath();
        initializeCsv();
    }

    /**
     * Resolves CSV file paths by checking for existing files.
     * 
     * Currently only checks backend/csvFiles/events.csv.
     * Sets both event and recurrent CSV paths to the backend folder.
     */
    private void resolveCsvPath() {
        String[] possiblePaths = {
                "backend/csvFiles/events.csv",
        };

        for (String path : possiblePaths) {
            File f = new File(path);
            if (f.exists()) {
                csvPath = path;
                recurrentCsvPath = "backend/csvFiles/recurrent.csv"; // Assume same dir
                System.out.println("DEBUG: Found existing CSV at " + csvPath);
                return;
            }
        }

        // Default
        csvPath = "backend/csvFiles/events.csv";
        recurrentCsvPath = "backend/csvFiles/recurrent.csv";
    }

    /**
     * Initializes both CSV files (events and recurrent).
     */
    private void initializeCsv() {
        // 1. Initialize Events CSV
        initializeFile(csvPath, "id,userId,title,description,startDateTime,endDateTime,category\n");
        // 2. Initialize Recurrent CSV
        initializeFile(recurrentCsvPath, "eventId,recurrentInterval,recurrentTimes,recurrentEndDate\n");
    }

    /**
     * Initializes a single CSV file with header.
     * 
     * Creates parent directories if needed and writes the header row.
     * Logs directory/file creation for debugging.
     * 
     * @param path   Path to the CSV file
     * @param header Header row to write (e.g., "id,title,description")
     */
    private void initializeFile(String path, String header) {
        if (path == null)
            return;
        File file = new File(path);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            System.out.println("DEBUG: Created directory " + parentDir.getAbsolutePath() + " -> " + created);
        }
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                System.out.println("DEBUG: Created file " + file.getAbsolutePath() + " -> " + created);
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write(header);
                }
            } catch (IOException e) {
                System.out.println("Error initializing CSV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads all events from the CSV file using Jackson.
     * 
     * Automatically filters out invalid events:
     * - Events with ID = 0 or userId = 0
     * - Events without title or startDateTime
     * - Malformed CSV rows
     * 
     * Thread-safe operation.
     * 
     * @return List of valid Event objects (empty if file doesn't exist)
     */
    public synchronized List<Event> loadEvents() {
        if (csvPath == null)
            return new ArrayList<>();

        File file = new File(csvPath);
        if (!file.exists() || file.length() == 0)
            return new ArrayList<>();

        List<Event> baseEvents = new ArrayList<>();
        try (MappingIterator<Event> it = mapper.readerFor(Event.class)
                .with(eventSchema)
                .readValues(file)) {
            while (it.hasNext()) {
                try {
                    Event event = it.next();
                    // Validate event has required fields
                    if (event.getId() != 0 && event.getUserId() != 0
                            && event.getTitle() != null && !event.getTitle().isEmpty()
                            && event.getStartDateTime() != null) {
                        baseEvents.add(event);
                    } else {
                        System.out.println("Skipping invalid event: " + event.getTitle());
                    }
                } catch (Exception e) {
                    System.out.println("Skipping malformed event entry: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV: " + e.getMessage());
            return new ArrayList<>();
        }

        return baseEvents;
    }

    /**
     * Generates the next available event ID.
     * 
     * Finds maximum ID from all loaded events and returns max + 1.
     * Thread-safe.
     * 
     * @return Next unique event ID (1 if no events exist)
     */
    public synchronized int getNextId() {
        List<Event> events = loadEvents();
        if (events.isEmpty())
            return 1;
        return events.stream()
                .mapToInt(Event::getId)
                .max()
                .orElse(0) + 1;
    }

    /**
     * Saves a new event to the CSV file.
     * 
     * Process:
     * 1. Loads all valid existing events
     * 2. Auto-generates next ID for the new event
     * 3. Validates event (userId, title, startDateTime required)
     * 4. Adds event to list
     * 5. Rewrites entire CSV file
     * 
     * Throws IllegalArgumentException if validation fails.
     * Thread-safe operation.
     * 
     * @param event The event to save (ID will be auto-assigned)
     * @throws IllegalArgumentException if event has invalid required fields
     */
    public synchronized void saveEvent(Event event) {
        // Load only VALID events (corrupted ones will be filtered out)
        List<Event> validEvents = loadValidEventsForWriting();

        // Auto-generate ID if needed (or overwrite if exists? Original code generated
        // new ID always)
        int newId = 0;
        if (!validEvents.isEmpty()) {
            newId = validEvents.stream().mapToInt(Event::getId).max().orElse(0);
        }
        event.setId(newId + 1);

        // Validate the event before saving
        if (event.getUserId() == 0) {
            System.err.println("ERROR: Cannot save event with userId=0. Event: " + event.getTitle());
            throw new IllegalArgumentException("Event must have a valid userId");
        }
        if (event.getTitle() == null || event.getTitle().isEmpty()) {
            System.err.println("ERROR: Cannot save event with empty title");
            throw new IllegalArgumentException("Event must have a title");
        }
        if (event.getStartDateTime() == null) {
            System.err.println("ERROR: Cannot save event with null startDateTime");
            throw new IllegalArgumentException("Event must have a start date/time");
        }

        // Debug: Log what we're about to save
        System.out.println("DEBUG: Saving event - ID: " + event.getId() +
                ", UserID: " + event.getUserId() +
                ", Title: " + event.getTitle() +
                ", Start: " + event.getStartDateTime());

        validEvents.add(event);

        writeEventsToCsv(validEvents);
        System.out.println("DEBUG: Successfully wrote " + validEvents.size() + " events to CSV");
    }

    /**
     * Escapes special CSV characters in a string value.
     * 
     * Wraps value in quotes if it contains: comma, quote, newline, or carriage
     * return.
     * Doubles any internal quotes for proper CSV escaping.
     * 
     * @param value The string to escape (null becomes empty string)
     * @return CSV-safe escaped string
     */
    private static String csvEscape(String value) {
        if (value == null) {
            return "";
        }

        boolean mustQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        if (!mustQuote) {
            return value;
        }

        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    /**
     * Writes a list of events to the CSV file.
     * 
     * Overwrites the entire file with header + all events.
     * Uses custom CSV escaping for special characters.
     * Thread-safe operation.
     * 
     * @param events The complete list of events to write
     */
    private synchronized void writeEventsToCsv(List<Event> events) {
        if (csvPath == null) {
            return;
        }

        File file = new File(csvPath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileWriter fw = new FileWriter(file, false)) {
            fw.write("id,userId,title,description,startDateTime,endDateTime,category\n");
            for (Event event : events) {
                String start = event.getStartDateTime() != null ? event.getStartDateTime().toString() : "";
                String end = event.getEndDateTime() != null ? event.getEndDateTime().toString() : "";
                String category = event.getCategory() != null ? event.getCategory() : "";

                fw.write(
                        event.getId() + "," +
                                event.getUserId() + "," +
                                csvEscape(event.getTitle()) + "," +
                                csvEscape(event.getDescription()) + "," +
                                start + "," +
                                end + "," +
                                csvEscape(category) + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error writing to CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads only valid events for write operations.
     * 
     * Filters out events with:
     * - ID = 0 or userId = 0
     * - Empty/null title
     * - Null startDateTime
     * - Malformed CSV data
     * 
     * This defensive method ensures corrupted data doesn't propagate.
     * Thread-safe.
     * 
     * @return List of validated events safe for writing
     */
    private synchronized List<Event> loadValidEventsForWriting() {
        if (csvPath == null)
            return new ArrayList<>();

        File file = new File(csvPath);
        if (!file.exists() || file.length() == 0)
            return new ArrayList<>();

        List<Event> validEvents = new ArrayList<>();
        try (MappingIterator<Event> it = mapper.readerFor(Event.class)
                .with(eventSchema)
                .readValues(file)) {
            while (it.hasNext()) {
                try {
                    Event event = it.next();
                    // Only keep events with valid required fields
                    if (event.getId() != 0 && event.getUserId() != 0
                            && event.getTitle() != null && !event.getTitle().isEmpty()
                            && event.getStartDateTime() != null) {
                        validEvents.add(event);
                    } else {
                        System.out.println("DEBUG: Filtering out invalid event during write: ID=" + event.getId()
                                + ", UserID=" + event.getUserId());
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG: Skipping malformed event during write: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV for writing: " + e.getMessage());
        }

        return validEvents;
    }

    /**
     * Saves recurrence metadata for an event.
     * 
     * Appends recurrence rules to recurrent.csv file.
     * 
     * @param eventId  The event's ID
     * @param interval Recurrence interval code (e.g., "1d", "1w", "1m")
     * @param times    Number of occurrences
     * @param endDate  End date for recurrence (can be null)
     */
    private void saveRecurrence(int eventId, String interval, String times, String endDate) {
        if (recurrentCsvPath == null)
            return;

        File file = new File(recurrentCsvPath);
        // Simple append mode or load-and-save? Load-and-save is safer with Jackson
        // But for recurrence simple POJO or List<Map> is fine.
        // Let's create a temporary inner class or use Map

        List<java.util.Map<String, Object>> recurrences = new ArrayList<>();
        if (file.exists() && file.length() > 0) {
            try (MappingIterator<java.util.Map<String, Object>> it = mapper.readerFor(java.util.Map.class)
                    .with(recurrentSchema)
                    .readValues(file)) {
                recurrences = it.readAll();
            } catch (IOException e) {
                // ignore
            }
        }

        java.util.Map<String, Object> recData = new java.util.HashMap<>();
        recData.put("eventId", eventId);
        recData.put("recurrentInterval", interval);
        recData.put("recurrentTimes", times);
        recData.put("recurrentEndDate", endDate);

        recurrences.add(recData);

        try {
            mapper.writer(recurrentSchema)
                    .writeValue(file, recurrences);
        } catch (IOException e) {
            System.out.println("Error writing to Recurrent CSV: " + e.getMessage());
        }
    }

    /**
     * Generates and saves multiple recurring event instances.
     * 
     * Process:
     * 1. Validates base event
     * 2. Saves base event with auto-assigned ID
     * 3. Saves recurrence metadata if interval is set
     * 4. Generates recurring instances based on interval:
     * - 1d: Daily
     * - 1w: Weekly
     * - 1m: Monthly
     * - 1y: Yearly
     * 5. Stops when:
     * - Reaches endDate (if set)
     * - Reaches occurrence limit (if times is set)
     * - Reaches default limit of 50 instances
     * 
     * All instances share the same title, description, category, and user.
     * Thread-safe operation.
     * 
     * @param baseEvent The base event with recurrence parameters set
     */
    public synchronized void generateAndSaveRecurringEvents(Event baseEvent) {
        String intervalCode = baseEvent.getRecurrentInterval();
        String timesStr = baseEvent.getRecurrentTimes();

        // Load existing events to determine IDs and append new ones
        List<Event> validEvents = loadValidEventsForWriting();

        int nextId = 0;
        if (!validEvents.isEmpty()) {
            nextId = validEvents.stream().mapToInt(Event::getId).max().orElse(0) + 1;
        } else {
            nextId = 1;
        }

        // Prepare Base Event
        baseEvent.setId(nextId++);

        // Basic Validation
        if (baseEvent.getUserId() == 0 || baseEvent.getTitle() == null || baseEvent.getTitle().isEmpty()
                || baseEvent.getStartDateTime() == null) {
            System.err.println("ERROR: Invalid event data for recursion");
            return;
        }

        validEvents.add(baseEvent);

        // Save recurrence meta if exists
        if (intervalCode != null && !intervalCode.equals("None") && !intervalCode.isEmpty()) {
            saveRecurrence(baseEvent.getId(), intervalCode, timesStr, baseEvent.getRecurrentEndDate());

            int times = 0;
            try {
                if (timesStr != null && !timesStr.isEmpty()) {
                    times = Integer.parseInt(timesStr);
                }
            } catch (NumberFormatException e) {
            }

            LocalDateTime endDate = null;
            if (baseEvent.getRecurrentEndDate() != null && !baseEvent.getRecurrentEndDate().isEmpty()) {
                try {
                    endDate = java.time.LocalDate.parse(baseEvent.getRecurrentEndDate()).atStartOfDay().plusDays(1)
                            .minusNanos(1);
                } catch (Exception e) {
                }
            }

            LocalDateTime currentStart = baseEvent.getStartDateTime();
            LocalDateTime currentEnd = baseEvent.getEndDateTime();

            int count = 1;
            // Generate instances
            while (true) {
                if ("1d".equals(intervalCode)) {
                    currentStart = currentStart.plusDays(1);
                    if (currentEnd != null)
                        currentEnd = currentEnd.plusDays(1);
                } else if ("1w".equals(intervalCode)) {
                    currentStart = currentStart.plusWeeks(1);
                    if (currentEnd != null)
                        currentEnd = currentEnd.plusWeeks(1);
                } else if ("1m".equals(intervalCode)) {
                    currentStart = currentStart.plusMonths(1);
                    if (currentEnd != null)
                        currentEnd = currentEnd.plusMonths(1);
                } else if ("1y".equals(intervalCode)) {
                    currentStart = currentStart.plusYears(1);
                    if (currentEnd != null)
                        currentEnd = currentEnd.plusYears(1);
                } else {
                    break;
                }

                if (endDate != null) {
                    if (currentStart.isAfter(endDate))
                        break;
                } else if (times > 0) {
                    count++;
                    if (count > times)
                        break;
                } else {
                    if (count++ > 50)
                        break; // Default limit if no condition
                }

                Event newEvent = new Event();
                newEvent.setId(nextId++);
                newEvent.setUserId(baseEvent.getUserId());
                newEvent.setTitle(baseEvent.getTitle());
                newEvent.setDescription(baseEvent.getDescription());
                newEvent.setCategory(baseEvent.getCategory());
                newEvent.setStartDateTime(currentStart);
                newEvent.setEndDateTime(currentEnd);

                validEvents.add(newEvent);
            }
        }

        writeEventsToCsv(validEvents);
        System.out.println("DEBUG: Generated recurring events. Total events saved: " + validEvents.size());
    }

    /**
     * Updates an existing event.
     * 
     * Loads all valid events, finds the matching event by ID,
     * replaces it, and rewrites the CSV file.
     * 
     * Preserves the original event ID.
     * Thread-safe operation.
     * 
     * @param eventId      The ID of the event to update
     * @param updatedEvent The event with new data
     */
    public synchronized void updateEvent(int eventId, Event updatedEvent) {
        List<Event> events = loadValidEventsForWriting(); // Use valid events only
        boolean found = false;

        // Find and update the event
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId() == eventId) {
                updatedEvent.setId(eventId); // Preserve the ID
                events.set(i, updatedEvent);
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Event with ID " + eventId + " not found for update");
            return;
        }

        writeEventsToCsv(events);
        System.out.println("Event " + eventId + " updated successfully");
    }

    /**
     * Deletes an event by ID.
     * 
     * Also deletes any associated recurrence rules.
     * Thread-safe operation.
     * 
     * @param eventId The ID of the event to delete
     */
    public synchronized void deleteEvent(int eventId) {
        List<Event> events = loadValidEventsForWriting(); // Use valid events only
        boolean removed = events.removeIf(e -> e.getId() == eventId);

        if (!removed) {
            System.out.println("Event with ID " + eventId + " not found for deletion");
            return;
        }

        writeEventsToCsv(events);
        System.out.println("Event " + eventId + " deleted successfully");

        // Also remove any recurrence rules for this event
        deleteRecurrenceRule(eventId);
    }

    /**
     * Deletes recurrence metadata for an event.
     * 
     * Removes the event's entry from recurrent.csv.
     * Thread-safe operation.
     * 
     * @param eventId The event ID whose recurrence rules should be deleted
     */
    private synchronized void deleteRecurrenceRule(int eventId) {
        if (recurrentCsvPath == null)
            return;

        File file = new File(recurrentCsvPath);
        if (!file.exists() || file.length() == 0)
            return;

        List<java.util.Map<String, Object>> recurrences = new ArrayList<>();
        try (MappingIterator<java.util.Map<String, Object>> it = mapper.readerFor(java.util.Map.class)
                .with(recurrentSchema)
                .readValues(file)) {
            recurrences = it.readAll();
        } catch (IOException e) {
            return;
        }

        // Remove recurrence rule for this event
        recurrences.removeIf(rec -> {
            try {
                int recEventId = Integer.parseInt(String.valueOf(rec.get("eventId")));
                return recEventId == eventId;
            } catch (Exception e) {
                return false;
            }
        });

        // Write back
        try {
            mapper.writer(recurrentSchema)
                    .writeValue(file, recurrences);
        } catch (IOException e) {
            System.out.println("Error deleting recurrence rule: " + e.getMessage());
        }
    }
}
