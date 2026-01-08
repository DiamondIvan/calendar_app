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
            baseEvents = it.readAll();
        } catch (IOException e) {
            System.out.println("Error reading CSV: " + e.getMessage());
            return new ArrayList<>();
        }

        // Expand Recurrences
        List<Event> allEvents = new ArrayList<>(baseEvents);
        java.util.Map<Integer, java.util.Map<String, String>> rules = loadRecurrenceRules();

        for (Event base : baseEvents) {
            if (rules.containsKey(base.getId())) {
                expandRecurrence(base, rules.get(base.getId()), allEvents);
            }
        }

        return allEvents;
    }

    private java.util.Map<Integer, java.util.Map<String, String>> loadRecurrenceRules() {
        java.util.Map<Integer, java.util.Map<String, String>> rules = new java.util.HashMap<>();
        if (recurrentCsvPath == null)
            return rules;
        File file = new File(recurrentCsvPath);
        if (!file.exists() || file.length() == 0)
            return rules;

        try (MappingIterator<java.util.Map<String, String>> it = mapper.readerFor(java.util.Map.class)
                .with(recurrentSchema)
                .readValues(file)) {
            while (it.hasNext()) {
                java.util.Map<String, String> row = it.next();
                try {
                    int eventId = Integer.parseInt(String.valueOf(row.get("eventId")));
                    rules.put(eventId, row);
                } catch (Exception e) {
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading Recurrent CSV: " + e.getMessage());
        }
        return rules;
    }

    private void expandRecurrence(Event base, java.util.Map<String, String> rule, List<Event> targetList) {
        String intervalCode = rule.get("recurrentInterval");
        String timesStr = rule.get("recurrentTimes");
        // String endDateStr = rule.get("recurrentEndDate"); // Not used yet

        int times = 0;
        try {
            if (timesStr != null && !timesStr.isEmpty()) {
                times = Integer.parseInt(timesStr);
            }
        } catch (Exception e) {
        }

        if (intervalCode == null || "None".equalsIgnoreCase(intervalCode) || times <= 0)
            return;

        LocalDateTime currentStart = base.getStartDateTime();
        LocalDateTime currentEnd = base.getEndDateTime();

        for (int i = 1; i < times; i++) {
            if (intervalCode.equals("1d")) {
                currentStart = currentStart.plusDays(1);
                currentEnd = currentEnd.plusDays(1);
            } else if (intervalCode.equals("1w")) {
                currentStart = currentStart.plusWeeks(1);
                currentEnd = currentEnd.plusWeeks(1);
            } else if (intervalCode.equals("1m")) {
                currentStart = currentStart.plusMonths(1);
                currentEnd = currentEnd.plusMonths(1);
            } else if (intervalCode.equals("1y")) {
                currentStart = currentStart.plusYears(1);
                currentEnd = currentEnd.plusYears(1);
            }

            Event recurringEvent = new Event();
            // Use same ID or negative? Logic usually implies unique ID for UI interactions.
            // For display, unique ID is better. But linking back is hard.
            // Let's generate a temporary unique ID hash or something.
            recurringEvent.setId(base.getId() + (i * 100000)); // Hacky unique ID
            recurringEvent.setUserId(base.getUserId());
            recurringEvent.setTitle(base.getTitle());
            recurringEvent.setDescription(base.getDescription());
            recurringEvent.setCategory(base.getCategory());
            recurringEvent.setStartDateTime(currentStart);
            recurringEvent.setEndDateTime(currentEnd);

            targetList.add(recurringEvent);
        }
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
        List<Event> events = loadEvents();

        // Auto-generate ID if needed (or overwrite if exists? Original code generated
        // new ID always)
        int newId = 0;
        if (!events.isEmpty()) {
            newId = events.stream().mapToInt(Event::getId).max().orElse(0);
        }
        event.setId(newId + 1);

        events.add(event);

        try {
            mapper.writer(eventSchema)
                    .writeValue(new File(csvPath), events);
        } catch (IOException e) {
            System.out.println("Error writing to CSV: " + e.getMessage());
        }
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

        // Save original event first
        saveEvent(baseEvent);

        // Save recurrence meta if exists
        if (intervalCode != null && !intervalCode.equals("None")) {
            saveRecurrence(baseEvent.getId(), intervalCode, timesStr, baseEvent.getRecurrentEndDate());
        }
    }
}
