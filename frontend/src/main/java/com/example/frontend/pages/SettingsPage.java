package com.example.frontend.pages;

import com.example.frontend.ApiService;
import com.example.frontend.context.ThemeManager;
import com.example.frontend.model.AppUser;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SettingsPage {

    private final Consumer<String> navigate;
    private AppUser currentUser;
    private final ApiService apiService;

    public SettingsPage() {
        this(null, null);
    }

    public SettingsPage(Consumer<String> navigate) {
        this(navigate, null);
    }

    public SettingsPage(Consumer<String> navigate, AppUser currentUser) {
        this.navigate = navigate;
        this.currentUser = currentUser;
        this.apiService = new ApiService();
    }

    public Node getView() {
        // Check if user is logged in
        if (currentUser == null || currentUser.getId() == null || currentUser.getId() <= 0) {
            return new LoginRequiredPage(navigate, "Please log in to access settings", "login", "Go to Login")
                    .getView();
        }

        ThemeManager theme = ThemeManager.getInstance();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20, 40, 20, 40));
        root.getStyleClass().add("settings-container");

        // Apply theme background
        theme.applyBackground(root);

        try {
            root.getStylesheets()
                    .add(getClass().getResource("/frontend/CSS_SubPage/SettingsPage.css").toExternalForm());
        } catch (Exception e) {
            // ignore
        }

        if (navigate != null) {
            root.getChildren().add(new com.example.frontend.components.NavBar(navigate));
        }

        // Header
        Label title = new Label("Settings");
        title.getStyleClass().add("settings-title");

        String subtitleText = "Manage your account and preferences";
        if (currentUser != null && currentUser.getName() != null && !currentUser.getName().isBlank()) {
            subtitleText = "Account settings for " + currentUser.getName();
        }

        Label subtitle = new Label(subtitleText);
        subtitle.getStyleClass().add("settings-subtitle");
        subtitle.setWrapText(true);

        // Sections Container
        VBox sectionsContainer = new VBox(25);
        sectionsContainer.setAlignment(Pos.CENTER);
        sectionsContainer.setMaxWidth(Double.MAX_VALUE);
        sectionsContainer.prefWidthProperty().bind(root.widthProperty().multiply(0.50));

        // Profile Section
        VBox profileSection = createProfileSection();

        // Security Section
        VBox securitySection = createSecuritySection();

        // Account Actions Section
        VBox actionsSection = createAccountActionsSection();

        sectionsContainer.getChildren().addAll(profileSection, securitySection, actionsSection);

        root.getChildren().addAll(title, subtitle, sectionsContainer);

        scrollPane.setContent(root);
        return scrollPane;
    }

    private VBox createProfileSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("settings-section");
        section.setMaxWidth(Double.MAX_VALUE);

        ThemeManager theme = ThemeManager.getInstance();

        Label sectionTitle = new Label("👤 Profile Information");
        sectionTitle.getStyleClass().add("section-title");

        // Name Field
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Name:");
        nameLabel.getStyleClass().add("field-label");
        nameLabel.setMinWidth(80);

        TextField nameField = new TextField();
        nameField.getStyleClass().add("settings-input");
        nameField.setPromptText("Enter your name");
        if (currentUser != null && currentUser.getName() != null) {
            nameField.setText(currentUser.getName());
        }
        nameField.setMaxWidth(300);

        nameBox.getChildren().addAll(nameLabel, nameField);

        // Email Field (Read-only)
        HBox emailBox = new HBox(10);
        emailBox.setAlignment(Pos.CENTER_LEFT);
        Label emailLabel = new Label("Email:");
        emailLabel.getStyleClass().add("field-label");
        emailLabel.setMinWidth(80);

        TextField emailField = new TextField();
        emailField.getStyleClass().add("settings-input");
        emailField.setEditable(false);
        emailField.setStyle("-fx-opacity: 0.7;");
        if (currentUser != null && currentUser.getEmail() != null) {
            emailField.setText(currentUser.getEmail());
        }
        emailField.setMaxWidth(300);

        emailBox.getChildren().addAll(emailLabel, emailField);

        // Update Profile Button
        Button updateProfileBtn = new Button("💾 Update Profile");
        updateProfileBtn.getStyleClass().add("settings-button");
        applyThemeToButton(updateProfileBtn, theme);
        updateProfileBtn.setOnAction(e -> updateProfile(nameField.getText()));

        section.getChildren().addAll(sectionTitle, nameBox, emailBox, updateProfileBtn);
        return section;
    }

    private VBox createSecuritySection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("settings-section");
        section.setMaxWidth(Double.MAX_VALUE);

        ThemeManager theme = ThemeManager.getInstance();

        Label sectionTitle = new Label("🔒 Security");
        sectionTitle.getStyleClass().add("section-title");

        // Current Password
        HBox currentPwBox = new HBox(10);
        currentPwBox.setAlignment(Pos.CENTER_LEFT);
        Label currentPwLabel = new Label("Current Password:");
        currentPwLabel.getStyleClass().add("field-label");
        currentPwLabel.setMinWidth(110);

        PasswordField currentPwField = new PasswordField();
        currentPwField.getStyleClass().add("settings-input");
        currentPwField.setPromptText("Enter current password");
        currentPwField.setMaxWidth(280);

        currentPwBox.getChildren().addAll(currentPwLabel, currentPwField);

        // New Password
        HBox newPwBox = new HBox(10);
        newPwBox.setAlignment(Pos.CENTER_LEFT);
        Label newPwLabel = new Label("New Password:");
        newPwLabel.getStyleClass().add("field-label");
        newPwLabel.setMinWidth(110);

        PasswordField newPwField = new PasswordField();
        newPwField.getStyleClass().add("settings-input");
        newPwField.setPromptText("Enter new password");
        newPwField.setMaxWidth(280);

        newPwBox.getChildren().addAll(newPwLabel, newPwField);

        // Confirm Password
        HBox confirmPwBox = new HBox(10);
        confirmPwBox.setAlignment(Pos.CENTER_LEFT);
        Label confirmPwLabel = new Label("Confirm Password:");
        confirmPwLabel.getStyleClass().add("field-label");
        confirmPwLabel.setMinWidth(110);

        PasswordField confirmPwField = new PasswordField();
        confirmPwField.getStyleClass().add("settings-input");
        confirmPwField.setPromptText("Confirm new password");
        confirmPwField.setMaxWidth(280);

        confirmPwBox.getChildren().addAll(confirmPwLabel, confirmPwField);

        // Change Password Button
        Button changePwBtn = new Button("🔑 Change Password");
        changePwBtn.getStyleClass().add("settings-button");
        applyThemeToButton(changePwBtn, theme);
        changePwBtn.setOnAction(e -> changePassword(
                currentPwField.getText(),
                newPwField.getText(),
                confirmPwField.getText(),
                currentPwField,
                newPwField,
                confirmPwField));

        section.getChildren().addAll(sectionTitle, currentPwBox, newPwBox, confirmPwBox, changePwBtn);
        return section;
    }

    private VBox createAccountActionsSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("settings-section");
        section.setMaxWidth(Double.MAX_VALUE);

        ThemeManager theme = ThemeManager.getInstance();

        Label sectionTitle = new Label("⚡ Account Actions");
        sectionTitle.getStyleClass().add("section-title");

        // Buttons Container
        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER);

        // Logout Button
        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.getStyleClass().add("settings-button");
        applyThemeToLogoutButton(logoutBtn, theme);
        logoutBtn.setOnAction(e -> logout());

        // Delete Account Button
        Button deleteBtn = new Button("🗑️ Delete Account");
        deleteBtn.getStyleClass().add("settings-button");
        applyThemeToDeleteButton(deleteBtn);
        deleteBtn.setOnAction(e -> deleteAccount());

        buttonsBox.getChildren().addAll(logoutBtn, deleteBtn);

        Label warningLabel = new Label("⚠️ Warning: Deleting your account is permanent and cannot be undone.");
        warningLabel.getStyleClass().add("warning-label");
        warningLabel.setWrapText(true);
        warningLabel.setMaxWidth(600);

        section.getChildren().addAll(sectionTitle, buttonsBox, warningLabel);
        return section;
    }

    private void updateProfile(String newName) {
        if (currentUser == null) {
            showAlert("Error", "No user logged in", Alert.AlertType.ERROR);
            return;
        }

        if (newName == null || newName.trim().isEmpty()) {
            showAlert("Error", "Name cannot be empty", Alert.AlertType.ERROR);
            return;
        }

        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", currentUser.getId());
            userData.put("name", newName.trim());
            userData.put("email", currentUser.getEmail());
            userData.put("password", currentUser.getPassword());

            JsonNode response = apiService.putJson("/api/users/" + currentUser.getId(), userData);

            if (response.has("success") && response.get("success").asBoolean()) {
                currentUser.setName(newName.trim());
                showAlert("Success", "Profile updated successfully!", Alert.AlertType.INFORMATION);
            } else {
                String message = response.has("message") ? response.get("message").asText()
                        : "Failed to update profile";
                showAlert("Error", message, Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to update profile: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void changePassword(String currentPw, String newPw, String confirmPw,
            PasswordField currentField, PasswordField newField, PasswordField confirmField) {
        if (currentUser == null) {
            showAlert("Error", "No user logged in", Alert.AlertType.ERROR);
            return;
        }

        // Validate inputs
        if (currentPw == null || currentPw.trim().isEmpty()) {
            showAlert("Error", "Please enter your current password", Alert.AlertType.ERROR);
            return;
        }

        if (newPw == null || newPw.trim().isEmpty()) {
            showAlert("Error", "Please enter a new password", Alert.AlertType.ERROR);
            return;
        }

        if (!newPw.equals(confirmPw)) {
            showAlert("Error", "New passwords do not match", Alert.AlertType.ERROR);
            return;
        }

        if (!currentPw.equals(currentUser.getPassword())) {
            showAlert("Error", "Current password is incorrect", Alert.AlertType.ERROR);
            return;
        }

        if (newPw.length() < 3) {
            showAlert("Error", "New password must be at least 3 characters long", Alert.AlertType.ERROR);
            return;
        }

        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", currentUser.getId());
            userData.put("name", currentUser.getName());
            userData.put("email", currentUser.getEmail());
            userData.put("password", newPw);

            JsonNode response = apiService.putJson("/api/users/" + currentUser.getId(), userData);

            if (response.has("success") && response.get("success").asBoolean()) {
                currentUser.setPassword(newPw);
                currentField.clear();
                newField.clear();
                confirmField.clear();
                showAlert("Success", "Password changed successfully!", Alert.AlertType.INFORMATION);
            } else {
                String message = response.has("message") ? response.get("message").asText()
                        : "Failed to change password";
                showAlert("Error", message, Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to change password: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteAccount() {
        if (currentUser == null) {
            showAlert("Error", "No user logged in", Alert.AlertType.ERROR);
            return;
        }

        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Account");
        confirmation.setHeaderText("Are you absolutely sure?");
        confirmation.setContentText("This will permanently delete your account and all associated data.\n" +
                "This action cannot be undone!\n\n" +
                "User: " + currentUser.getEmail());

        ButtonType deleteButton = new ButtonType("Delete Forever", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmation.getButtonTypes().setAll(deleteButton, cancelButton);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                try {
                    JsonNode result = apiService.deleteJson("/api/users/" + currentUser.getId());

                    if (result.has("success") && result.get("success").asBoolean()) {
                        showAlert("Account Deleted", "Your account has been permanently deleted.",
                                Alert.AlertType.INFORMATION);
                        logout();
                    } else {
                        String message = result.has("message") ? result.get("message").asText()
                                : "Failed to delete account";
                        showAlert("Error", message, Alert.AlertType.ERROR);
                    }
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete account: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void logout() {
        if (navigate != null) {
            navigate.accept("/logout");
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void applyThemeToButton(Button button, ThemeManager theme) {
        String buttonColor = theme.getButtonColor();
        String hoverColor = theme.getHoverButtonColor();

        String normalStyle = "-fx-background-color: " + buttonColor + ";" +
                " -fx-text-fill: white;" +
                " -fx-font-size: 14px;" +
                " -fx-font-weight: bold;" +
                " -fx-padding: 12 30;" +
                " -fx-background-radius: 8;" +
                " -fx-cursor: hand;";

        String hoverStyle = "-fx-background-color: " + hoverColor + ";" +
                " -fx-text-fill: white;" +
                " -fx-font-size: 14px;" +
                " -fx-font-weight: bold;" +
                " -fx-padding: 12 30;" +
                " -fx-background-radius: 8;" +
                " -fx-cursor: hand;";

        button.setStyle(normalStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
    }

    private void applyThemeToLogoutButton(Button button, ThemeManager theme) {
        // Use theme color for logout button too
        applyThemeToButton(button, theme);
    }

    private void applyThemeToDeleteButton(Button button) {
        // Keep red color for delete button (danger action)
        String normalStyle = "-fx-background-color: #e74c3c;" +
                " -fx-text-fill: white;" +
                " -fx-font-size: 14px;" +
                " -fx-font-weight: bold;" +
                " -fx-padding: 12 30;" +
                " -fx-background-radius: 8;" +
                " -fx-cursor: hand;";

        String hoverStyle = "-fx-background-color: #c0392b;" +
                " -fx-text-fill: white;" +
                " -fx-font-size: 14px;" +
                " -fx-font-weight: bold;" +
                " -fx-padding: 12 30;" +
                " -fx-background-radius: 8;" +
                " -fx-cursor: hand;";

        button.setStyle(normalStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
    }
}
