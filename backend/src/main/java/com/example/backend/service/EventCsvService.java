package com.example.backend.service;

import com.example.backend.model.Event;
import com.example.backend.utils.BackendPaths;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.time.LocalDateTime;

public class EventCsvService {

    private final Path csvPath = BackendPaths.resolveBackendDir().resolve("csvFiles").resolve("events.csv");

    public EventCsvService() {
        initializeCsv();
    }

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

    public void generateAndSaveRecurringEvents(Event event) {
        saveEvent(event);
    }

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

    public void deleteEvent(int id) {
        List<Event> allEvents = loadEvents();
        boolean removed = allEvents.removeIf(e -> e.getId() == id);

        if (removed) {
            rewriteCsv(allEvents);
        }
    }

    public void deleteEventsByUserId(int userId) {
        List<Event> allEvents = loadEvents();
        boolean removed = allEvents.removeIf(e -> e.getUserId() == userId);

        if (removed) {
            rewriteCsv(allEvents);
        }
    }

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
