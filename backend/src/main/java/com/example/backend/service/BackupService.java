package com.example.backend.service;

import com.example.backend.utils.BackendPaths;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * BackupService manages backup and restore operations for the calendar
 * application.
 * 
 * This service provides comprehensive data protection:
 * - **Backup**: Creates unified CSV backup files containing all data
 * - **Restore**: Restores data from backups (replace or append mode)
 * - **List**: Retrieves available backup files
 * 
 * Backup file format:
 * - Single CSV file with section markers (#EVENTS, #RECURRENTS, #USERS)
 * - Version marker (#BACKUP_VERSION=1) for future compatibility
 * - UTF-8 encoding for international character support
 * - Each section has its own header row
 * 
 * Data included in backups:
 * 1. Events (id, userId, title, description, dates, category)
 * 2. Recurrent rules (eventId, interval, times, endDate)
 * 3. Users (id, name, email, password)
 * 
 * Location: backend/backups/
 * 
 * **Warning**: Append mode may cause ID collisions. Use with caution.
 */
public class BackupService {

    /** Base directory containing all backend files */
    private final Path baseDir;

    /** Path to events.csv */
    private final Path eventFile;

    /** Path to recurrent.csv */
    private final Path recurrentFile;

    /** Path to users.csv */
    private final Path userFile;

    /** Directory where backup files are stored */
    private final Path backupDir;

    /** CSV header for events data */
    public static final String EVENT_HEADER = "id,userId,title,description,startDateTime,endDateTime,category";

    /** CSV header for recurrent rules */
    public static final String RECURRENT_HEADER = "eventId,recurrentInterval,recurrentTimes,recurrentEndDate";

    /** CSV header for users data */
    public static final String USERS_HEADER = "id,name,email,password";

    /**
     * Constructs a BackupService and initializes backup directory.
     * 
     * Sets up paths to:
     * - events.csv
     * - recurrent.csv
     * - users.csv
     * - backups/ directory (created if doesn't exist)
     */
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

    /**
     * Creates a backup of all application data.
     * 
     * Process:
     * 1. Generates filename (auto if not provided)
     * 2. Appends .csv extension if missing
     * 3. Creates backup file with version marker
     * 4. Writes all data in sections:
     * - #EVENTS section with events data
     * - #RECURRENTS section with recurrence rules
     * - #USERS section with user accounts
     * 
     * Each section includes its header and data (excluding source file headers).
     * Uses UTF-8 encoding for international characters.
     * 
     * @param backupName Optional backup filename (auto-generated if null/empty)
     * @return Absolute path to the created backup file
     * @throws IOException If file write fails
     */
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

    /**
     * Copies data from a source CSV file to backup, excluding the header.
     * 
     * Filtering:
     * - Skips first line (header)
     * - Skips empty lines
     * - Skips lines with BOM (\uFEFF)
     * - Trims whitespace
     * 
     * Used internally by backupEvents() for each data file.
     * 
     * @param sourceFile Path to the source CSV file
     * @param backupOut  PrintWriter to write data to
     * @throws IOException If reading source file fails
     */
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

    /**
     * Restores data from a backup file.
     * 
     * Process:
     * 1. Validates backup file exists
     * 2. Reads backup file and parses sections:
     * - #EVENTS → event data
     * - #RECURRENTS → recurrence rules
     * - #USERS → user accounts
     * 3. Writes data to respective CSV files
     * 
     * **Restore Modes:**
     * - **Replace** (append=false): Overwrites existing data completely
     * - **Append** (append=true): Adds backup data to existing data
     * ⚠️ Warning: May cause ID collisions and data corruption
     * 
     * Skips:
     * - Empty lines
     * - Version markers (#BACKUP_VERSION)
     * - Section markers starting with #
     * 
     * @param backupName Name of the backup file to restore from
     * @param append     True to append data, false to replace existing data
     * @throws IOException           If file read/write fails
     * @throws FileNotFoundException If backup file doesn't exist
     */
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

    /**
     * Writes data lines to a CSV file with header.
     * 
     * Header behavior:
     * - Written if: append=false OR file doesn't exist OR file is empty
     * - Skipped if: append=true AND file exists with content
     * 
     * Uses UTF-8 encoding.
     * Filters out empty lines.
     * 
     * @param filePath Path to the target CSV file
     * @param header   Header row to write (can be null)
     * @param lines    Data lines to write
     * @param append   True to append to file, false to overwrite
     * @throws IOException If file write fails
     */
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

    /**
     * Lists all available backup files.
     * 
     * Scans the backups/ directory for .csv files.
     * 
     * @return List of backup filenames (just names, not full paths)
     */
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

    /**
     * Returns the backup directory as a File object.
     * 
     * @return The backups/ directory
     */
    public File getBackupDirectory() {
        return backupDir.toFile();
    }
}