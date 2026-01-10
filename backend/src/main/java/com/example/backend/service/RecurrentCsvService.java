package com.example.backend.service;

import com.example.backend.model.Event;
import com.example.backend.utils.BackendPaths;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class RecurrentCsvService {

    private final Path csvPath = BackendPaths.resolveBackendDir().resolve("csvFiles").resolve("recurrent.csv");

    public RecurrentCsvService() {
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
                fw.write("eventId,recurrentInterval,recurrentTimes,recurrentEndDate\n");
            } catch (IOException e) {
                System.out.println("Error initializing recurrent CSV: " + e.getMessage());
            }
        }
    }

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

    public void deleteRecurrentRule(int id) {
        List<Event> rules = loadRecurrentRules();
        boolean removed = rules.removeIf(e -> e.getId() == id);
        if (removed) {
            rewriteCsv(rules);
        }
    }

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