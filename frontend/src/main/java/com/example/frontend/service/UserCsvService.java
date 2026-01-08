package com.example.frontend.service;

import com.example.frontend.model.AppUser;

import java.io.*;
import java.util.*;

public class UserCsvService {

    private String csvPath;

    public UserCsvService() {
        resolveCsvPath();
        System.out.println("UserCsvService initialized. CSV Path: " + new File(csvPath).getAbsolutePath());
        initializeCsv();
    }

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

    public boolean emailExists(String email) {
        List<AppUser> users = loadUsers();
        for (AppUser user : users) {
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

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
