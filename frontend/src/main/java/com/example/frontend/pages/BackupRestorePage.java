package com.example.frontend.pages;

import com.example.frontend.ApiService;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.ConnectException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BackupRestorePage provides UI for backing up and restoring event data.
 * 
 * This page communicates with the backend API to:
 * - Create backups of event data with optional custom filenames
 * - List available backup files
 * - Restore data from backups with two modes:
 * - Replace: Overwrites existing events
 * - Append: Adds backup events to existing data
 * 
 * Features:
 * - Asynchronous operations using JavaFX Tasks
 * - Real-time status messages (success/error)
 * - Automatic backup list refresh after creation
 * - Error handling with user-friendly messages
 * - Backend connectivity checks
 */
public class BackupRestorePage {

    /** Text field for entering custom backup filename */
    private TextField backupNameField;

    /** Dropdown for selecting which backup file to restore */
    private ComboBox<String> restoreFileBox;

    /** Radio button for "replace existing events" restore mode */
    private RadioButton replaceRadio;

    /** Radio button for "append to existing" restore mode */
    private RadioButton appendRadio;

    /** Label for displaying status messages to the user */
    private Label messageLabel;

    /** Button to trigger backup creation */
    private Button backupBtn;

    /** Button to trigger data restoration */
    private Button restoreBtn;

    /** API service for communicating with the backend */
    private final ApiService apiService = new ApiService();

    /** Navigation callback for routing to other pages */
    private java.util.function.Consumer<String> navigate;

    /**
     * Default constructor with no navigation.
     */
    public BackupRestorePage() {
    }

    /**
     * Constructor with navigation callback.
     * 
     * @param navigate Navigation callback for routing between pages
     */
    public BackupRestorePage(java.util.function.Consumer<String> navigate) {
        this.navigate = navigate;
    }

    /**
     * Builds and returns the backup/restore page UI.
     * 
     * The page layout includes:
     * - Header with title and subtitle
     * - Navigation bar (if navigate callback is provided)
     * - Message label for status updates
     * - Backup section:
     * - Optional filename input field
     * - Create backup button
     * - Restore section:
     * - Dropdown to select backup file
     * - Radio buttons for restore mode (replace/append)
     * - Restore button
     * 
     * On load, automatically refreshes the list of available backups.
     * 
     * @return A BorderPane containing the complete backup/restore UI
     */
    public Node getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("backup-restore-container");
        root.setMaxWidth(800);

        try {
            root.getStylesheets()
                    .add(getClass().getResource("/frontend/CSS_SubPage/BackupRestorePage.css").toExternalForm());
        } catch (Exception e) {
        }

        // Content Area
        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        // Header
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        Label title = new Label("Backup & Restore");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Manage your calendar data safely");
        subtitle.getStyleClass().add("subtitle");
        header.getChildren().addAll(title, subtitle);

        // NavBar (Placed below header)
        com.example.frontend.components.NavBar navBar = null;
        if (navigate != null) {
            navBar = new com.example.frontend.components.NavBar(navigate);
            navBar.setStyle(
                    "-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
            navBar.setMaxWidth(600);
            navBar.setAlignment(Pos.CENTER);
        }

        // Message Area
        messageLabel = new Label();
        messageLabel.setVisible(false);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.getStyleClass().add("message-label");

        // Backup Section
        VBox backupSection = new VBox(10);
        backupSection.getStyleClass().add("section");

        Label backupHeader = new Label("Backup Events");
        backupHeader.getStyleClass().add("section-header");
        backupHeader.setMaxWidth(Double.MAX_VALUE);

        Label backupNameLbl = new Label("Backup Filename (Optional):");
        backupNameLbl.getStyleClass().add("form-label");

        backupNameField = new TextField();
        backupNameField.setPromptText("e.g., my_backup");
        backupNameField.getStyleClass().add("text-field");

        backupBtn = new Button("Create Backup");
        backupBtn.getStyleClass().add("primary-btn");
        backupBtn.setOnAction(e -> handleBackup());

        backupSection.getChildren().addAll(backupHeader, backupNameLbl, backupNameField, backupBtn);

        // Restore Section
        VBox restoreSection = new VBox(10);
        restoreSection.getStyleClass().add("section");

        Label restoreHeader = new Label("Restore Events");
        restoreHeader.getStyleClass().add("section-header");
        restoreHeader.setMaxWidth(Double.MAX_VALUE);

        Label restoreFileLbl = new Label("Select Backup File:");
        restoreFileLbl.getStyleClass().add("form-label");

        restoreFileBox = new ComboBox<>();
        restoreFileBox.setPromptText("Select a file...");
        restoreFileBox.setDisable(true);
        restoreFileBox.setMaxWidth(Double.MAX_VALUE);
        restoreFileBox.getStyleClass().add("combo-box");

        Label modeLabel = new Label("Restore Mode:");
        modeLabel.getStyleClass().add("form-label");

        ToggleGroup modeGroup = new ToggleGroup();
        replaceRadio = new RadioButton("Replace existing events");
        replaceRadio.setToggleGroup(modeGroup);
        replaceRadio.setSelected(true);

        appendRadio = new RadioButton("Append to existing events");
        appendRadio.setToggleGroup(modeGroup);

        HBox radioBox = new HBox(20, replaceRadio, appendRadio);

        restoreBtn = new Button("Restore Data");
        restoreBtn.getStyleClass().add("primary-btn");
        restoreBtn.setOnAction(e -> handleRestore());

        restoreSection.getChildren().addAll(restoreHeader, restoreFileLbl, restoreFileBox, modeLabel, radioBox,
                restoreBtn);

        // Assemble Content
        content.getChildren().add(header);
        if (navBar != null) {
            content.getChildren().add(navBar);
        }
        content.getChildren().addAll(messageLabel, backupSection, restoreSection);

        root.setCenter(content);

        refreshBackupList();
        return root;
    }

    /**
     * Handles the backup creation process.
     * 
     * Steps:
     * 1. Retrieves optional backup name from the text field
     * 2. Creates a JSON payload with the backup name
     * 3. Disables UI controls and shows "Creating backup..." message
     * 4. Sends asynchronous POST request to /api/backup/create
     * 5. On success:
     * - Shows success message
     * - Refreshes backup list
     * 6. On failure:
     * - Shows formatted error message
     * 7. Re-enables UI controls
     */
    private void handleBackup() {
        String name = backupNameField.getText() == null ? "" : backupNameField.getText().trim();
        Map<String, Object> payload = new HashMap<>();
        payload.put("backupName", name);

        setBusy(true);
        showMessage("Creating backup...", true);

        Task<JsonNode> task = new Task<>() {
            @Override
            protected JsonNode call() throws Exception {
                return apiService.postJson("/api/backup/create", payload);
            }
        };

        task.setOnSucceeded(e -> {
            JsonNode response = task.getValue();
            boolean success = response != null && response.path("success").asBoolean(false);
            String msg = response == null
                    ? "Backup failed."
                    : response.path("message").asText(success ? "Backup created successfully" : "Backup failed");

            showMessage(msg, success);
            setBusy(false);
            if (success) {
                refreshBackupList();
            }
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showMessage(formatBackendError("Backup failed", ex), false);
            setBusy(false);
        });

        Thread t = new Thread(task, "backup-create-task");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Handles the data restoration process.
     * 
     * Steps:
     * 1. Validates that a backup file is selected
     * 2. Determines restore mode (replace or append)
     * 3. Creates JSON payload with backup name and mode
     * 4. Disables UI controls and shows "Restoring data..." message
     * 5. Sends asynchronous POST request to /api/backup/restore
     * 6. On success/failure, shows appropriate message
     * 7. Re-enables UI controls
     */
    private void handleRestore() {
        String selected = restoreFileBox.getValue();
        if (selected == null) {
            showMessage("Please select a file to restore", false);
            return;
        }

        boolean append = appendRadio.isSelected();
        Map<String, Object> payload = new HashMap<>();
        payload.put("backupName", selected);
        payload.put("append", append);

        setBusy(true);
        showMessage("Restoring data...", true);

        Task<JsonNode> task = new Task<>() {
            @Override
            protected JsonNode call() throws Exception {
                return apiService.postJson("/api/backup/restore", payload);
            }
        };

        task.setOnSucceeded(e -> {
            JsonNode response = task.getValue();
            boolean success = response != null && response.path("success").asBoolean(false);
            String msg = response == null
                    ? "Restore failed."
                    : response.path("message").asText(success ? "Backup restored successfully" : "Restore failed");
            showMessage(msg, success);
            setBusy(false);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showMessage(formatBackendError("Restore failed", ex), false);
            setBusy(false);
        });

        Thread t = new Thread(task, "backup-restore-task");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Refreshes the list of available backup files from the backend.
     * 
     * Steps:
     * 1. Disables UI and shows "Loading backups..." message
     * 2. Sends asynchronous GET request to /api/backup/list
     * 3. Parses JSON response to extract backup file names
     * 4. On success:
     * - Populates the restore dropdown with backup names
     * - Enables the dropdown
     * - Shows "Backups loaded" or "No backups found" message
     * 5. On failure:
     * - Clears dropdown
     * - Shows error message
     * 6. Re-enables UI controls
     */
    private void refreshBackupList() {
        setBusy(true);
        showMessage("Loading backups...", true);

        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                JsonNode response = apiService.getJson("/api/backup/list");
                if (response == null || !response.path("success").asBoolean(false)) {
                    String msg = response == null ? "Failed to list backups"
                            : response.path("message").asText("Failed to list backups");
                    throw new IllegalStateException(msg);
                }

                List<String> names = new ArrayList<>();
                for (JsonNode backup : response.path("backups")) {
                    String fileName = backup.path("name").asText("");
                    if (!fileName.isBlank()) {
                        names.add(fileName);
                    }
                }
                return names;
            }
        };

        task.setOnSucceeded(e -> {
            List<String> names = task.getValue();
            restoreFileBox.getItems().setAll(names);
            restoreFileBox.setDisable(false);
            setBusy(false);

            if (names == null || names.isEmpty()) {
                showMessage("No backups found yet.", true);
            } else {
                showMessage("Backups loaded.", true);
            }
        });

        task.setOnFailed(e -> {
            restoreFileBox.getItems().clear();
            restoreFileBox.setDisable(false);
            Throwable ex = task.getException();
            showMessage(formatBackendError("Failed to load backups", ex), false);
            setBusy(false);
        });

        Thread t = new Thread(task, "backup-list-task");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Enables or disables UI controls during async operations.
     * 
     * When busy (operation in progress):
     * - Disables backup button
     * - Disables restore button
     * - Disables restore file dropdown
     * 
     * When not busy:
     * - Enables backup and restore buttons
     * 
     * @param busy True to disable controls, false to enable them
     */
    private void setBusy(boolean busy) {
        if (backupBtn != null) {
            backupBtn.setDisable(busy);
        }
        if (restoreBtn != null) {
            restoreBtn.setDisable(busy);
        }
        if (restoreFileBox != null && busy) {
            restoreFileBox.setDisable(true);
        }
    }

    /**
     * Displays a status message to the user.
     * 
     * Updates the message label with styled text.
     * Applies CSS classes for success (green) or error (red) styling.
     * 
     * @param msg     The message text to display
     * @param success True for success styling, false for error styling
     */
    private void showMessage(String msg, boolean success) {
        messageLabel.setText(msg);
        messageLabel.setVisible(true);
        messageLabel.getStyleClass().removeAll("success-message", "error-message");
        messageLabel.getStyleClass().add(success ? "success-message" : "error-message");
    }

    /**
     * Formats backend error messages in a user-friendly way.
     * 
     * Handles common error cases:
     * - Connection errors: Shows "Cannot reach backend" message
     * - Timeout errors: Shows connection failure message
     * - Other errors: Shows the root cause exception message
     * 
     * Traverses the exception chain to find the root cause.
     * 
     * @param prefix Prefix text for the error message (e.g., "Backup failed")
     * @param ex     The exception that occurred
     * @return A formatted, user-friendly error message
     */
    private String formatBackendError(String prefix, Throwable ex) {
        if (ex == null) {
            return prefix + ": Unknown error";
        }

        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }

        if (root instanceof ConnectException
                || root instanceof HttpConnectTimeoutException
                || root instanceof HttpTimeoutException) {
            return prefix + ": Cannot reach backend at http://localhost:8080. Start the backend server and try again.";
        }

        String msg = root.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = root.getClass().getSimpleName();
        }
        return prefix + ": " + msg;
    }
}
