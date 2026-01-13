package com.example.backend.service;

import com.example.backend.model.AppUser;
import com.example.backend.utils.BackendPaths;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * UserCsvService manages user data persistence for the backend using CSV file
 * storage.
 * 
 * This service handles all user-related database operations including:
 * - CRUD operations (Create, Read, Update, Delete)
 * - User authentication and validation
 * - Email uniqueness checking
 * - Auto-generating unique user IDs
 * - UTF-8 encoded CSV file management
 * 
 * File location: backend/csvFiles/users.csv
 * CSV format: id,name,email,password
 * 
 * All write operations use UTF-8 encoding to support international characters.
 * Thread-safe ID generation and update/delete operations.
 */
public class UserCsvService {

    /** Path to the users CSV file (resolved via BackendPaths utility) */
    private final Path csvPath = BackendPaths.resolveBackendDir().resolve("csvFiles").resolve("users.csv");

    /**
     * Constructs a UserCsvService and initializes the CSV file.
     * Prints the resolved CSV path to console and ensures the file exists.
     */
    public UserCsvService() {
        System.out.println("UserCsvService initialized. CSV Path: " + csvPath.toAbsolutePath());
        initializeCsv();
    }

    /**
     * Initializes the CSV file if it doesn't exist.
     * 
     * Creates the parent directory (csvFiles) and the users.csv file with header.
     * Uses UTF-8 encoding for international character support.
     * 
     * Header: "id,name,email,password"
     */
    private void initializeCsv() {
        File file = csvPath.toFile();
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        if (!file.exists()) {
            try (Writer fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                fw.write("id,name,email,password\n");
            } catch (IOException e) {
                System.out.println("Error initializing users CSV: " + e.getMessage());
            }
        }
    }

    /**
     * Loads all users from the CSV file.
     * 
     * Reads using UTF-8 encoding and parses each row into AppUser objects.
     * Skips invalid rows and continues processing.
     * 
     * @return List of all users (empty list if file doesn't exist or error occurs)
     */
    public List<AppUser> loadUsers() {
        List<AppUser> users = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(csvPath.toFile()), StandardCharsets.UTF_8))) {
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
     * Scans all existing users to find max ID and returns max + 1.
     * Thread-safe to prevent duplicate IDs during concurrent registration.
     * 
     * @return Next unique user ID (1 if no users exist)
     */
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

    /**
     * Saves a new user to the CSV file.
     * 
     * Auto-generates and assigns a new ID to the user.
     * Ensures proper newline before appending to prevent malformed CSV.
     * Uses UTF-8 encoding.
     * 
     * Logs each save operation to console for debugging.
     * 
     * @param user The user to save (ID will be auto-assigned)
     */
    public void saveUser(AppUser user) {
        System.out.println("Attempting to save user: " + user.getEmail());
        int newId = getNextId();
        user.setId(newId);
        System.out.println("Generated ID: " + newId);

        ensureNewline();

        try (Writer fw = new OutputStreamWriter(new FileOutputStream(csvPath.toFile(), true), StandardCharsets.UTF_8)) {
            String row = user.getId() + "," +
                    user.getName() + "," +
                    user.getEmail() + "," +
                    user.getPassword() + "\n";
            fw.write(row);
            System.out.println("Successfully wrote to CSV: " + row.trim());
        } catch (Exception e) {
            System.out.println("Error writing to CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ensures the CSV file ends with a newline character.
     * 
     * Checks the last byte of the file and appends a newline if needed.
     * This prevents CSV rows from being concatenated incorrectly.
     * 
     * Uses RandomAccessFile for efficient end-of-file checking.
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
                    // ignore
                }
            }
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Checks if an email address is already registered.
     * 
     * Performs exact string matching (case-sensitive in backend).
     * 
     * @param email The email to check (returns false if null)
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        if (email == null)
            return false;
        List<AppUser> users = loadUsers();
        for (AppUser user : users) {
            if (email.equals(user.getEmail())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates user login credentials.
     * 
     * Checks for exact email and password match.
     * 
     * @param email    User's email (returns null if null)
     * @param password User's password (returns null if null)
     * @return The AppUser object if valid, null otherwise
     */
    public AppUser validateUser(String email, String password) {
        if (email == null || password == null)
            return null;
        List<AppUser> users = loadUsers();
        for (AppUser user : users) {
            if (email.equals(user.getEmail()) && password.equals(user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Updates an existing user's information.
     * 
     * Loads all users, finds the matching user by ID, replaces it,
     * and rewrites the entire CSV file.
     * 
     * Thread-safe operation.
     * 
     * @param updatedUser The user with updated information (must have valid ID)
     * @return true if user was found and updated, false otherwise
     */
    public synchronized boolean updateUser(AppUser updatedUser) {
        if (updatedUser == null || updatedUser.getId() == 0) {
            return false;
        }

        List<AppUser> users = loadUsers();
        boolean found = false;

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == updatedUser.getId()) {
                users.set(i, updatedUser);
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        // Rewrite the entire CSV file
        try (Writer fw = new OutputStreamWriter(new FileOutputStream(csvPath.toFile()), StandardCharsets.UTF_8)) {
            fw.write("id,name,email,password\n");
            for (AppUser user : users) {
                String row = user.getId() + "," +
                        user.getName() + "," +
                        user.getEmail() + "," +
                        user.getPassword() + "\n";
                fw.write(row);
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a user by ID.
     * 
     * Loads all users, removes the matching user, and rewrites the CSV file.
     * Thread-safe operation.
     * 
     * @param userId The ID of the user to delete
     * @return true if user was found and deleted, false otherwise
     */
    public synchronized boolean deleteUser(int userId) {
        List<AppUser> users = loadUsers();
        boolean removed = users.removeIf(user -> user.getId() == userId);

        if (!removed) {
            return false;
        }

        // Rewrite the entire CSV file
        try (Writer fw = new OutputStreamWriter(new FileOutputStream(csvPath.toFile()), StandardCharsets.UTF_8)) {
            fw.write("id,name,email,password\n");
            for (AppUser user : users) {
                String row = user.getId() + "," +
                        user.getName() + "," +
                        user.getEmail() + "," +
                        user.getPassword() + "\n";
                fw.write(row);
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a user by their ID.
     * 
     * @param userId The ID of the user to find
     * @return The AppUser object if found, null otherwise
     */
    public AppUser getUserById(int userId) {
        List<AppUser> users = loadUsers();
        for (AppUser user : users) {
            if (user.getId() == userId) {
                return user;
            }
        }
        return null;
    }
}
