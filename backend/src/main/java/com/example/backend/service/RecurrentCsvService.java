package com.example.backend.service;

import com.example.backend.model.Event;
import com.example.backend.utils.BackendPaths;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * RecurrentCsvService manages recurring event rules in CSV storage.
 * 
 * This service handles the metadata for recurring events:
 * - Stores recurrence patterns (interval, frequency, end conditions)
 * - Links to base events via eventId
 * - Supports CRUD operations on recurrence rules
 * - Bulk deletion by event IDs
 * 
 * File location: backend/csvFiles/recurrent.csv
 * CSV format: eventId,recurrentInterval,recurrentTimes,recurrentEndDate
 * 
 * Recurrence intervals:
 * - "1d" = Daily
 * - "1w" = Weekly
 * - "1m" = Monthly
 * - "1y" = Yearly
 * 
 * End conditions:
 * - recurrentTimes: Number of occurrences (e.g., "10")
 * - recurrentEndDate: End date (e.g., "2026-12-31")
 * - Both can be set; whichever comes first stops recurrence
 * 
 * Note: Actual event instance generation happens in EventCsvService.
 * This service only stores the rules.
 */
public class RecurrentCsvService {

    /** Path to the recurrent.csv file */
    private final Path csvPath = BackendPaths.resolveBackendDir().resolve("csvFiles").resolve("recurrent.csv");

    /**
     * Constructs a RecurrentCsvService and initializes the CSV file.
     */
    public RecurrentCsvService() {
        initializeCsv();
    }

    /**
     * Initializes the recurrent.csv file if it doesn't exist.
     * 
     * Creates parent directory and writes CSV header with UTF-8 encoding.
     * Header: "eventId,recurrentInterval,recurrentTimes,recurrentEndDate"
     */
    private void initializeCsv() {
        File file = csvPath.toFile();
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        if (!file.exists()) {
            try (Writer fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                fw.write("eventId,recurrentInterval,recurrentTimes,recurrentEndDate\n");
            } catch (IOException e) {
                System.out.println("Error initializing recurrent CSV: " + e.getMessage());
            }
        }
    }

    /**
     * Loads all recurrence rules from the CSV file.
     * 
     * Reads using UTF-8 encoding and parses each row into Event objects.
     * Only recurrence-related fields are populated:
     * - id (event ID this rule applies to)
     * - recurrentInterval (e.g., "1d", "1w")
     * - recurrentTimes (number of occurrences)
     * - recurrentEndDate (end date string)
     * 
     * Skips rows with invalid event IDs or fewer than 4 fields.
     * 
     * @return List of Event objects containing recurrence rules (empty if error)
     */
    public List<Event> loadRecurrentRules() {
        List<Event> recurrentRules = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(csvPath.toFile()), StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",", -1);

                if (fields.length < 4)
                    continue;

                Event event = new Event();
                try {
                    event.setId(Integer.parseInt(fields[0]));
                } catch (NumberFormatException e) {
                    continue;
                }
                event.setRecurrentInterval(fields[1]);
                event.setRecurrentTimes(fields[2]);
                event.setRecurrentEndDate(fields[3]);

                recurrentRules.add(event);
            }
        } catch (Exception e) {
            System.out.println("Error reading recurrent CSV: " + e.getMessage());
        }

        return recurrentRules;
    }

    /**
     * Saves a new recurrence rule to the CSV file.
     * 
     * Appends the rule to the file using the event's:
     * - id (as eventId)
     * - recurrentInterval
     * - recurrentTimes
     * - recurrentEndDate
     * 
     * Ensures proper newline before appending.
     * Uses UTF-8 encoding.
     * 
     * @param event Event containing recurrence rule data
     */
    public void saveRecurrentRule(Event event) {
        ensureNewline();
        try (Writer fw = new OutputStreamWriter(new FileOutputStream(csvPath.toFile(), true), StandardCharsets.UTF_8)) {
            fw.write(
                    event.getId() + "," +
                            event.getRecurrentInterval() + "," +
                            event.getRecurrentTimes() + "," +
                            event.getRecurrentEndDate() +
                            "\n");
        } catch (Exception e) {
            System.out.println("Error writing recurrent CSV: " + e.getMessage());
        }
    }

    /**
     * Ensures the CSV file ends with a newline character.
     * 
     * Checks the last byte and appends newline if needed.
     * Prevents malformed CSV rows.
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
     * Updates an existing recurrence rule or creates it if not found.
     * 
     * Behavior:
     * - If rule with matching ID exists: Updates it
     * - If rule not found AND recurrentInterval is set: Creates new rule
     * - If rule not found AND no interval: Does nothing
     * 
     * This allows updating event recurrence patterns after creation.
     * 
     * @param id           The event ID
     * @param updatedEvent Event containing new recurrence data
     */
    public void updateRecurrentRule(int id, Event updatedEvent) {
        List<Event> rules = loadRecurrentRules();
        boolean found = false;
        for (int i = 0; i < rules.size(); i++) {
            if (rules.get(i).getId() == id) {
                updatedEvent.setId(id);
                rules.set(i, updatedEvent);
                found = true;
                break;
            }
        }

        if (found) {
            rewriteCsv(rules);
        } else if (updatedEvent.getRecurrentInterval() != null && !updatedEvent.getRecurrentInterval().isEmpty()) {
            saveRecurrentRule(updatedEvent);
        }
    }

    /**
     * Deletes a recurrence rule by event ID.
     * 
     * Removes the rule from the CSV file and rewrites remaining rules.
     * 
     * @param id The event ID whose recurrence rule should be deleted
     */
    public void deleteRecurrentRule(int id) {
        List<Event> rules = loadRecurrentRules();
        boolean removed = rules.removeIf(e -> e.getId() == id);
        if (removed) {
            rewriteCsv(rules);
        }
    }

    /**
     * Deletes multiple recurrence rules by event IDs.
     * 
     * Bulk deletion operation for cleaning up rules when events are deleted.
     * Useful when deleting a user's events - also removes their recurrence rules.
     * 
     * @param eventIds List of event IDs whose rules should be deleted (null-safe)
     */
    public void deleteRecurrentRulesByEventIds(List<Integer> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return;
        }

        List<Event> rules = loadRecurrentRules();
        boolean removed = rules.removeIf(e -> eventIds.contains(e.getId()));

        if (removed) {
            rewriteCsv(rules);
        }
    }

    /**
     * Rewrites the entire CSV file with the provided rules.
     * 
     * Used after update/delete operations to persist changes.
     * Writes header and all rules with UTF-8 encoding.
     * 
     * @param rules The complete list of recurrence rules to write
     */
    private void rewriteCsv(List<Event> rules) {
        try (Writer fw = new OutputStreamWriter(new FileOutputStream(csvPath.toFile(), false),
                StandardCharsets.UTF_8)) {
            fw.write("eventId,recurrentInterval,recurrentTimes,recurrentEndDate\n");
            for (Event event : rules) {
                fw.write(
                        event.getId() + "," +
                                event.getRecurrentInterval() + "," +
                                event.getRecurrentTimes() + "," +
                                event.getRecurrentEndDate() +
                                "\n");
            }
        } catch (Exception e) {
            System.out.println("Error rewriting recurrent CSV: " + e.getMessage());
        }
    }
}