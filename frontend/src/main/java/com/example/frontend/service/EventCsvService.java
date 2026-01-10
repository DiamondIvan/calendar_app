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

public class EventCsvService {

    private String csvPath;
    private String recurrentCsvPath;
    private final CsvMapper mapper;
    private final CsvSchema eventSchema;
    private final CsvSchema recurrentSchema;

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

    private void initializeCsv() {
        // 1. Initialize Events CSV
        initializeFile(csvPath, "id,userId,title,description,startDateTime,endDateTime,category\n");
        // 2. Initialize Recurrent CSV
        initializeFile(recurrentCsvPath, "eventId,recurrentInterval,recurrentTimes,recurrentEndDate\n");
    }

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

    // Load all events from CSV
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

    public synchronized int getNextId() {
        List<Event> events = loadEvents();
        if (events.isEmpty())
            return 1;
        return events.stream()
                .mapToInt(Event::getId)
                .max()
                .orElse(0) + 1;
    }

    // Append a single event to CSV
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

    // Load only valid events for writing back to CSV (filters out corrupted
    // entries)
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

    // Update an existing event
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

    // Delete an event by ID
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

    // Delete recurrence rule for an event
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
