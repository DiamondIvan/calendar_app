package com.example.frontend.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class BackupRestorePage {

    private TextField backupNameField;
    private ComboBox<String> restoreFileBox;
    private RadioButton replaceRadio;
    private RadioButton appendRadio;
    private Label messageLabel;

    private java.util.function.Consumer<String> navigate;

    public BackupRestorePage() {
    }

    public BackupRestorePage(java.util.function.Consumer<String> navigate) {
        this.navigate = navigate;
    }

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

        Button backupBtn = new Button("Create Backup");
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
        restoreFileBox.getItems().addAll("backup_2026-01-01.csv", "backup_final.csv"); // Mock items
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

        Button restoreBtn = new Button("Restore Data");
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
        return root;
    }

    private void handleBackup() {
        // Mock Backup
        String name = backupNameField.getText().isEmpty() ? "default_backup" : backupNameField.getText();
        System.out.println("Backing up to: " + name);

        showMessage("Backup created successfully: " + name + ".csv", true);

        // Add to list mock
        restoreFileBox.getItems().add(name + ".csv");
    }

    private void handleRestore() {
        String selected = restoreFileBox.getValue();
        if (selected == null) {
            showMessage("Please select a file to restore", false);
            return;
        }

        boolean append = appendRadio.isSelected();
        System.out.println("Restoring from " + selected + " (Append: " + append + ")");

        showMessage("Data restored successfully from " + selected, true);
    }

    private void showMessage(String msg, boolean success) {
        messageLabel.setText(msg);
        messageLabel.setVisible(true);
        messageLabel.getStyleClass().removeAll("success-message", "error-message");
        messageLabel.getStyleClass().add(success ? "success-message" : "error-message");
    }
}
