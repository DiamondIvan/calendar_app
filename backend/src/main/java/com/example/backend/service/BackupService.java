package com.example.backend.service;

import com.example.backend.utils.BackendPaths;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BackupService {

    private final Path baseDir;
    private final Path eventFile;
    private final Path recurrentFile;
    private final Path userFile;
    private final Path backupDir;

    public static final String EVENT_HEADER = "id,userId,title,description,startDateTime,endDateTime,category";
    public static final String RECURRENT_HEADER = "eventId,recurrentInterval,recurrentTimes,recurrentEndDate";
    public static final String USERS_HEADER = "id,name,email,password";

    public BackupService() {
        this.baseDir = BackendPaths.resolveBackendDir();
        this.eventFile = baseDir.resolve("csvFiles").resolve("events.csv");
        this.recurrentFile = baseDir.resolve("csvFiles").resolve("recurrent.csv");
        this.userFile = baseDir.resolve("csvFiles").resolve("users.csv");
        this.backupDir = baseDir.resolve("backups");

        File dir = backupDir.toFile();
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

        File backupFile = backupDir.resolve(backupName).toFile();

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(backupFile), StandardCharsets.UTF_8))) {
            out.println("#BACKUP_VERSION=1");
            out.println("#EVENTS");
            out.println(EVENT_HEADER);
            copyDataWithoutHeader(eventFile, out);

            out.println("#RECURRENTS");
            out.println(RECURRENT_HEADER);
            copyDataWithoutHeader(recurrentFile, out);

            out.println("#USERS");
            out.println(USERS_HEADER);
            copyDataWithoutHeader(userFile, out);
        }

        return backupFile.getAbsolutePath();
    }

    private void copyDataWithoutHeader(Path sourceFile, PrintWriter backupOut) throws IOException {
        File source = sourceFile.toFile();
        if (!source.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(source), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                // Skip empty lines and BOM
                if (line.isEmpty() || line.startsWith("\uFEFF")) {
                    continue;
                }

                // Skip header line
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

        File backupFile = backupDir.resolve(backupName).toFile();
        if (!backupFile.exists()) {
            throw new FileNotFoundException("Backup file not found: " + backupName);
        }

        // Warning: Append mode can cause ID collisions and broken relationships
        // For production use, implement ID remapping logic
        if (append) {
            System.err.println("WARNING: Append mode may cause ID collisions. Use with caution.");
        }

        List<String> eventData = new ArrayList<>();
        List<String> recurrentData = new ArrayList<>();
        List<String> userData = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(backupFile), StandardCharsets.UTF_8))) {
            String line;
            int mode = 0; // 0 = none, 1 = events, 2 = recurrents, 3 = users

            while ((line = br.readLine()) != null) {
                line = line.trim();

                // Skip empty lines and version markers
                if (line.isEmpty() || line.startsWith("#BACKUP_VERSION")) {
                    continue;
                }

                if (line.equals("#EVENTS")) {
                    mode = 1;
                    br.readLine();
                    continue;
                } else if (line.equals("#RECURRENTS")) {
                    mode = 2;
                    br.readLine();
                    continue;
                } else if (line.equals("#USERS")) {
                    mode = 3;
                    br.readLine();
                    continue;
                }

                // Skip section markers that might be headers
                if (line.startsWith("#")) {
                    continue;
                }

                if (mode == 1 && !line.isEmpty()) {
                    eventData.add(line);
                } else if (mode == 2 && !line.isEmpty()) {
                    recurrentData.add(line);
                } else if (mode == 3 && !line.isEmpty()) {
                    userData.add(line);
                }
            }
        }

        writeLinesToCsv(eventFile, EVENT_HEADER, eventData, append);
        writeLinesToCsv(recurrentFile, RECURRENT_HEADER, recurrentData, append);
        writeLinesToCsv(userFile, USERS_HEADER, userData, append);
    }

    private void writeLinesToCsv(Path filePath, String header, List<String> lines, boolean append)
            throws IOException {
        File file = filePath.toFile();
        boolean writeHeader = !append || !file.exists() || file.length() == 0;

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file, append), StandardCharsets.UTF_8))) {
            if (writeHeader && header != null) {
                out.println(header);
            }
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    out.println(line);
                }
            }
        }
    }

    public List<String> listBackups() {
        File folder = backupDir.toFile();
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

    public File getBackupDirectory() {
        return backupDir.toFile();
    }
}