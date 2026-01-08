package com.example.backend.service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BackupService {

    private static final String EVENT_FILE = "backend/csvFiles/events.csv";
    private static final String RECURRENT_FILE = "backend/csvFiles/recurrent.csv";
    private static final String BACKUP_DIR = "backend/backups/";

    public static final String EVENT_HEADER = "id,userId,title,description,startDateTime,endDateTime";
    public static final String RECURRENT_HEADER = "eventId,recurrentInterval,recurrentTimes,reccurentEndDate";

    public BackupService() {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String backupEvents(String backupName) throws IOException {
        if (backupName == null || backupName.trim().isEmpty()) {
            backupName = "backup_" + System.currentTimeMillis() + ".csv";
        }
        if (!backupName.endsWith(".csv")) {
            backupName += ".csv";
        }

        File backupFile = new File(BACKUP_DIR + backupName);

        try (PrintWriter out = new PrintWriter(new FileWriter(backupFile))) {
            out.println("#EVENTS");
            out.println(EVENT_HEADER);
            copyDataWithoutHeader(EVENT_FILE, out);

            out.println("#RECURRENTS");
            out.println(RECURRENT_HEADER);
            copyDataWithoutHeader(RECURRENT_FILE, out);
        }

        return backupFile.getAbsolutePath();
    }

    private void copyDataWithoutHeader(String sourceFileName, PrintWriter backupOut) throws IOException {
        File source = new File(sourceFileName);
        if (!source.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(source))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                backupOut.println(line);
            }
        }
    }

    public void restoreEvents(String backupName, boolean append) throws IOException {
        if (!backupName.endsWith(".csv")) {
            backupName += ".csv";
        }

        File backupFile = new File(BACKUP_DIR + backupName);
        if (!backupFile.exists()) {
            throw new FileNotFoundException("Backup file not found: " + backupName);
        }

        List<String> eventData = new ArrayList<>();
        List<String> recurrentData = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(backupFile))) {
            String line;
            int mode = 0; // 0 = none, 1 = events, 2 = recurrents

            while ((line = br.readLine()) != null) {
                if (line.equals("#EVENTS")) {
                    mode = 1;
                    br.readLine();
                    continue;
                } else if (line.equals("#RECURRENTS")) {
                    mode = 2;
                    br.readLine();
                    continue;
                }

                if (mode == 1) {
                    eventData.add(line);
                } else if (mode == 2) {
                    recurrentData.add(line);
                }
            }
        }

        writeLinesToCsv(EVENT_FILE, EVENT_HEADER, eventData, append);
        writeLinesToCsv(RECURRENT_FILE, RECURRENT_HEADER, recurrentData, append);
    }

    private void writeLinesToCsv(String fileName, String header, List<String> lines, boolean append)
            throws IOException {
        File file = new File(fileName);
        boolean writeHeader = !append || !file.exists() || file.length() == 0;

        try (PrintWriter out = new PrintWriter(new FileWriter(file, append))) {
            if (writeHeader && header != null) {
                out.println(header);
            }
            for (String line : lines) {
                out.println(line);
            }
        }
    }

    public List<String> listBackups() {
        File folder = new File(BACKUP_DIR);
        File[] listOfFiles = folder.listFiles();
        List<String> backups = new ArrayList<>();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().endsWith(".csv")) {
                    backups.add(file.getName());
                }
            }
        }
        return backups;
    }
}