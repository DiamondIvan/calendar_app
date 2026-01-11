package com.example.backend.service;

import com.example.backend.model.AppUser;
import com.example.backend.utils.BackendPaths;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class UserCsvService {

    private final Path csvPath = BackendPaths.resolveBackendDir().resolve("csvFiles").resolve("users.csv");

    public UserCsvService() {
        System.out.println("UserCsvService initialized. CSV Path: " + csvPath.toAbsolutePath());
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
                fw.write("id,name,email,password\n");
            } catch (IOException e) {
                System.out.println("Error initializing users CSV: " + e.getMessage());
            }
        }
    }

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
