package com.example.backend.controllers;

import com.example.backend.service.BackupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for backup and restore operations.
 * 
 * Provides HTTP endpoints for:
 * - Creating backups of all application data
 * - Restoring data from backup files
 * - Listing available backups
 * - Deleting backup files
 * 
 * Base URL: /api/backup
 * 
 * All endpoints return JSON responses with:
 * - success: boolean
 * - message: string (description)
 * - Additional data fields as needed
 * 
 * Uses BackupService for all data operations.
 */
@RestController
@RequestMapping("/api/backup")
public class BackupController {

    /** Service handling backup/restore logic */
    private final BackupService backupService;

    /**
     * Constructs a BackupController with a new BackupService instance.
     */
    public BackupController() {
        this.backupService = new BackupService();
    }

    /**
     * Creates a backup of all application data.
     * 
     * Request body:
     * - backupName (optional): Custom backup filename
     * 
     * If backupName is not provided, generates: "backup_{timestamp}.csv"
     * 
     * Response (200 OK):
     * - success: true
     * - message: "Backup created successfully"
     * - backupPath: Absolute path to backup file
     * - backupName: Filename used
     * 
     * Response (500 Internal Server Error):
     * - success: false
     * - message: Error description
     * 
     * @param request Map containing optional backupName
     * @return ResponseEntity with backup details or error
     */
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
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating backup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Restores data from a backup file.
     * 
     * Request body:
     * - backupName (required): Name of backup file to restore
     * - append (optional, default false): True to append data, false to replace
     * 
     * Warning: Append mode may cause ID collisions.
     * 
     * Response (200 OK):
     * - success: true
     * - message: "Backup restored successfully"
     * 
     * Response (400 Bad Request):
     * - success: false
     * - message: "Backup name is required"
     * 
     * Response (500 Internal Server Error):
     * - success: false
     * - message: Error description
     * 
     * @param request Map with backupName and optional append flag
     * @return ResponseEntity with success status or error
     */
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

    /**
     * Lists all available backup files.
     * 
     * Scans the backups/ directory and returns metadata for each .csv file.
     * 
     * Response (200 OK):
     * - success: true
     * - backups: Array of backup objects, each containing:
     * - name: Filename
     * - size: File size in bytes
     * - lastModified: Timestamp (milliseconds since epoch)
     * - path: Absolute path
     * 
     * Response (500 Internal Server Error):
     * - success: false
     * - message: Error description
     * 
     * @return ResponseEntity with list of backups or error
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listBackups() {
        Map<String, Object> response = new HashMap<>();

        try {
            File backupDir = backupService.getBackupDirectory();
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

    /**
     * Deletes a backup file.
     * 
     * Path parameter:
     * - backupName: Name of backup file to delete (.csv extension optional)
     * 
     * Security: Sanitizes filename to prevent directory traversal.
     * 
     * Response (200 OK):
     * - success: true
     * - message: "Backup deleted successfully"
     * 
     * Response (404 Not Found):
     * - success: false
     * - message: "Backup file not found"
     * 
     * Response (500 Internal Server Error):
     * - success: false
     * - message: Error description
     * 
     * @param backupName Name of the backup file to delete
     * @return ResponseEntity with success status or error
     */
    @DeleteMapping("/{backupName}")
    public ResponseEntity<Map<String, Object>> deleteBackup(@PathVariable String backupName) {
        Map<String, Object> response = new HashMap<>();

        try {
            backupName = Paths.get(backupName).getFileName().toString();
            if (!backupName.endsWith(".csv")) {
                backupName += ".csv";
            }

            File backupFile = new File(backupService.getBackupDirectory(), backupName);
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
