package com.example.frontend.service;

import com.example.frontend.model.AppUser;

import java.io.*;
import java.util.*;

/**
 * UserCsvService manages user data persistence using CSV file storage.
 * 
 * This service handles all user-related file operations including:
 * - Reading and writing user data to users.csv
 * - Auto-generating unique user IDs
 * - User authentication and validation
 * - Email uniqueness checking
 * - CSV file initialization and path resolution
 * 
 * The CSV file format: id,name,email,password
 * 
 * File location is auto-detected from multiple possible paths:
 * - backend/csvFiles/users.csv
 * - ../backend/csvFiles/users.csv
 * - src/csv/users.csv
 * - backend/src/csv/users.csv
 * - ../backend/src/csv/users.csv
 */
public class UserCsvService {

    /** Path to the users CSV file (auto-resolved) */
    private String csvPath;

    /**
     * Constructs a UserCsvService and initializes the CSV file.
     * 
     * On construction:
     * 1. Resolves the CSV file path from multiple possible locations
     * 2. Prints the resolved absolute path to console
     * 3. Initializes the CSV file (creates if doesn't exist)
     */
    public UserCsvService() {
        resolveCsvPath();
        System.out.println("UserCsvService initialized. CSV Path: " + new File(csvPath).getAbsolutePath());
        initializeCsv();
    }

    /**
     * Resolves the CSV file path by checking multiple possible locations.
     * 
     * Searches for existing users.csv in the following order:
     * 1. backend/csvFiles/users.csv
     * 2. ../backend/csvFiles/users.csv
     * 3. src/csv/users.csv
     * 4. backend/src/csv/users.csv
     * 5. ../backend/src/csv/users.csv
     * 
     * If none exist, selects a default path based on directory structure:
     * - backend/src/csv/users.csv (if "backend" folder exists)
     * - ../backend/src/csv/users.csv (if "../backend" exists)
     * - src/csv/users.csv (fallback)
     */
    private void resolveCsvPath() {
        // Try to find the CSV file in various locations
        String[] possiblePaths = {
                "backend/csvFiles/users.csv",
                "../backend/csvFiles/users.csv",
                "src/csv/users.csv",
                "backend/src/csv/users.csv",
                "../backend/src/csv/users.csv"
        };

        for (String path : possiblePaths) {
            File f = new File(path);
            if (f.exists()) {
                csvPath = path;
                return;
            }
        }

        // If not found, default to creating it in a reasonable place
        // If "backend" folder exists, put it there
        if (new File("backend").exists()) {
            csvPath = "backend/src/csv/users.csv";
        } else if (new File("../backend").exists()) {
            csvPath = "../backend/src/csv/users.csv";
        } else {
            csvPath = "src/csv/users.csv";
        }
    }

    /**
     * Initializes the CSV file if it doesn't exist.
     * 
     * Creates necessary parent directories and writes the CSV header:
     * "id,name,email,password"
     * 
     * If the file already exists, no action is taken.
     */
    private void initializeCsv() {
        File file = new File(csvPath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        if (!file.exists()) {
            try (FileWriter fw = new FileWriter(file)) {
                fw.write("id,name,email,password\n");
            } catch (IOException e) {
                System.out.println("Error initializing users CSV: " + e.getMessage());
            }
        }
    }

    /**
     * Loads all users from the CSV file.
     * 
     * Reads the CSV file line by line, parsing each row into an AppUser object.
     * Skips the header row and any rows with fewer than 4 fields.
     * Continues processing even if individual rows fail to parse (defensive).
     * 
     * CSV format: id,name,email,password
     * 
     * @return List of AppUser objects loaded from CSV (empty list if file doesn't
     *         exist or error occurs)
     */
    public List<AppUser> loadUsers() {
        List<AppUser> users = new ArrayList<>();

        if (csvPath == null)
            return users;

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",", -1);

                if (fields.length < 4)
                    continue;

                AppUser user = new AppUser();
                try {
                    user.setId(Integer.parseInt(fields[0]));
                } catch (NumberFormatException e) {
                    continue;
                }
                user.setName(fields[1]);
                user.setEmail(fields[2]);
                user.setPassword(fields[3]);

                users.add(user);
            }
        } catch (Exception e) {
            System.out.println("Error reading CSV: " + e.getMessage());
        }

        return users;
    }

    /**
     * Generates the next available user ID.
     * 
     * Scans the CSV file to find the maximum existing ID and returns max + 1.
     * Thread-safe to prevent ID conflicts during concurrent access.
     * 
     * @return The next unique user ID (1 if file is empty or doesn't exist)
     */
    public synchronized int getNextId() {
        int maxId = 0;
        if (csvPath == null)
            return 1;

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
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
     * Checks if an email address already exists in the system.
     * 
     * Performs case-insensitive email comparison.
     * Useful for registration validation to prevent duplicate accounts.
     * 
     * @param email The email address to check
     * @return true if the email exists, false otherwise
     */
    public boolean emailExists(String email) {
        List<AppUser> users = loadUsers();
        for (AppUser user : users) {
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates user credentials for login.
     * 
     * Searches for a user with matching email (case-insensitive) and password
     * (exact match).
     * 
     * @param email    The user's email address
     * @param password The user's password (plain text)
     * @return The AppUser object if credentials are valid, null otherwise
     */
    public AppUser validateUser(String email, String password) {
        List<AppUser> users = loadUsers();
        for (AppUser user : users) {
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email) &&
                    user.getPassword() != null && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Saves a new user to the CSV file.
     * 
     * If the user's ID is not set or is 0, automatically generates and assigns
     * the next available ID.
     * 
     * Appends the user as a new row to the CSV file.
     * 
     * @param user The AppUser object to save (ID will be auto-assigned if not set)
     */
    public void saveUser(AppUser user) {
        if (csvPath == null)
            return;

        // If user ID is not set or 0, assign one
        if (user.getId() == null || user.getId() == 0) {
            user.setId(getNextId());
        }

        try (FileWriter fw = new FileWriter(csvPath, true)) {
            // id,name,email,password
            fw.write(String.format("%d,%s,%s,%s\n",
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPassword()));
        } catch (IOException e) {
            System.out.println("Error saving user: " + e.getMessage());
        }
    }
}
