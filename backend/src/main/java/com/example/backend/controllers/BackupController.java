package com.example.backend.controllers;

import com.example.backend.service.BackupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
@CrossOrigin(origins = "*")
public class BackupController {

    private final BackupService backupService;
    private static final String BACKUP_DIR = "backend/backups/";

    public BackupController() {
        this.backupService = new BackupService();
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBackup(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String backupName = request.get("backupName");
            if (backupName == null || backupName.trim().isEmpty()) {
                backupName = "backup_" + System.currentTimeMillis();
            }

            String backupPath = backupService.backupEvents(backupName);
            response.put("success", true);
            response.put("message", "Backup created successfully");
            response.put("backupPath", backupPath);
            response.put("backupName", backupName);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating backup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/restore")
    public ResponseEntity<Map<String, Object>> restoreBackup(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String backupName = (String) request.get("backupName");
            Boolean append = (Boolean) request.getOrDefault("append", false);

            if (backupName == null || backupName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Backup name is required");
                return ResponseEntity.badRequest().body(response);
            }

            backupService.restoreEvents(backupName, append);
            response.put("success", true);
            response.put("message", "Backup restored successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error restoring backup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listBackups() {
        Map<String, Object> response = new HashMap<>();

        try {
            File backupDir = new File(BACKUP_DIR);
            List<Map<String, Object>> backups = new ArrayList<>();

            if (backupDir.exists() && backupDir.isDirectory()) {
                File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".csv"));
                if (files != null) {
                    for (File file : files) {
                        Map<String, Object> backupInfo = new HashMap<>();
                        backupInfo.put("name", file.getName());
                        backupInfo.put("size", file.length());
                        backupInfo.put("lastModified", file.lastModified());
                        backupInfo.put("path", file.getAbsolutePath());
                        backups.add(backupInfo);
                    }
                }
            }

            response.put("success", true);
            response.put("backups", backups);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error listing backups: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{backupName}")
    public ResponseEntity<Map<String, Object>> deleteBackup(@PathVariable String backupName) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (!backupName.endsWith(".csv")) {
                backupName += ".csv";
            }

            File backupFile = new File(BACKUP_DIR + backupName);
            if (backupFile.exists()) {
                if (backupFile.delete()) {
                    response.put("success", true);
                    response.put("message", "Backup deleted successfully");
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "Failed to delete backup file");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            } else {
                response.put("success", false);
                response.put("message", "Backup file not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting backup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
