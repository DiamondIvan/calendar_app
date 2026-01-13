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

    /**
     * Default constructor for SettingsPage.
     * Initializes the page with no navigation callback and no current user.
     * This will delegate to the main constructor with both parameters as null.
     */
    public SettingsPage() {
        this(null, null);
    }

    /**
     * Constructor for SettingsPage with navigation callback.
     * Initializes the page with a navigation callback but no current user.
     * 
     * @param navigate A callback function that handles navigation to different
     *                 pages in the application.
     *                 Accepts a string representing the page route (e.g., "/login",
     *                 "/logout").
     */
    public SettingsPage(Consumer<String> navigate) {
        this(navigate, null);
    }

    /**
     * Main constructor for SettingsPage with navigation callback and current user.
     * Initializes all instance variables including the API service.
     * 
     * @param navigate    A callback function that handles navigation to different
     *                    pages in the application.
     *                    Can be null if navigation is not needed.
     * @param currentUser The currently logged-in user whose settings will be
     *                    displayed and managed.
     *                    Can be null if no user is logged in.
     */
    public SettingsPage(Consumer<String> navigate, AppUser currentUser) {
        this.navigate = navigate;
        this.currentUser = currentUser;
        this.apiService = new ApiService();
    }

    /**
     * Creates and returns the main view for the Settings Page.
     * This is the primary method that builds the entire settings UI.
     * 
     * The view includes:
     * - Login check: redirects to login page if user is not authenticated
     * - Navigation bar for moving between pages
     * - Page title and subtitle with user's name
     * - Three main sections: Profile, Security, and Account Actions
     * - Theme-aware styling and responsive layout
     * 
     * @return A JavaFX Node containing the complete settings page UI wrapped in a
     *         ScrollPane.
     *         If the user is not logged in, returns a LoginRequiredPage instead.
     */
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

    /**
     * Creates the Profile Information section of the settings page.
     * This section allows users to view and edit their profile details.
     * 
     * The section includes:
     * - A title with an emoji icon (👤 Profile Information)
     * - Name field: editable text field pre-populated with current user's name
     * - Email field: read-only text field showing current user's email (cannot be
     * changed)
     * - Update Profile button: saves changes to the user's name
     * 
     * @return A VBox containing all the profile information UI elements.
     */
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

    /**
     * Creates the Security section of the settings page.
     * This section allows users to change their password.
     * 
     * The section includes:
     * - A title with an emoji icon (🔒 Security)
     * - Current Password field: user must enter their existing password for
     * verification
     * - New Password field: user enters their desired new password
     * - Confirm Password field: user re-enters the new password to confirm it
     * matches
     * - Change Password button: triggers password update with validation
     * 
     * All password fields are masked (PasswordField) for security.
     * The button passes all three field values and references to the changePassword
     * method.
     * 
     * @return A VBox containing all the security/password change UI elements.
     */
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

    /**
     * Creates the Account Actions section of the settings page.
     * This section provides critical account management actions.
     * 
     * The section includes:
     * - A title with an emoji icon (⚡ Account Actions)
     * - Logout button: logs the user out and redirects to logout page
     * - Delete Account button: permanently deletes the user's account (styled in
     * red for danger)
     * - Warning label: informs users that account deletion is permanent and
     * irreversible
     * 
     * The buttons are horizontally arranged and use different styling:
     * - Logout uses the theme color
     * - Delete uses red (#e74c3c) to indicate a dangerous action
     * 
     * @return A VBox containing all the account action UI elements.
     */
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

    /**
     * Updates the user's profile name in the backend.
     * 
     * This method performs the following steps:
     * 1. Validates that a user is logged in
     * 2. Validates that the new name is not empty
     * 3. Sends a PUT request to the backend API with updated user data
     * 4. Updates the local currentUser object if successful
     * 5. Shows appropriate success or error messages
     * 
     * Note: The email and password are included in the request to maintain data
     * integrity,
     * but only the name is actually being changed.
     * 
     * @param newName The new name to update the profile with. Will be trimmed of
     *                whitespace.
     */
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

    /**
     * Changes the user's password with comprehensive validation.
     * 
     * This method performs the following validations and steps:
     * 1. Checks that a user is logged in
     * 2. Validates that current password is provided
     * 3. Validates that new password is provided
     * 4. Checks that new password and confirmation match
     * 5. Verifies that the current password is correct
     * 6. Ensures new password meets minimum length requirement (3 characters)
     * 7. Sends a PUT request to update the password in the backend
     * 8. Updates the local currentUser object if successful
     * 9. Clears all password fields for security
     * 10. Shows appropriate success or error messages
     * 
     * @param currentPw    The user's current password for verification
     * @param newPw        The new password the user wants to set
     * @param confirmPw    Confirmation of the new password (must match newPw)
     * @param currentField Reference to the current password field to clear it on
     *                     success
     * @param newField     Reference to the new password field to clear it on
     *                     success
     * @param confirmField Reference to the confirm password field to clear it on
     *                     success
     */
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

    /**
     * Permanently deletes the user's account with confirmation.
     * 
     * This method performs the following steps:
     * 1. Validates that a user is logged in
     * 2. Shows a confirmation dialog warning the user that:
     * - The action is permanent and cannot be undone
     * - All associated data will be deleted
     * - Displays the user's email for verification
     * 3. Provides two options: "Delete Forever" or "Cancel"
     * 4. If user confirms deletion:
     * - Sends a DELETE request to the backend API
     * - Shows success message if deletion succeeds
     * - Logs the user out automatically
     * 5. Shows error message if deletion fails
     * 
     * This is a destructive operation and requires explicit user confirmation.
     */
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

    /**
     * Logs out the current user by navigating to the logout page.
     * 
     * This method checks if a navigation callback exists and, if so,
     * navigates to the "/logout" route. The actual logout logic
     * (clearing session, user data, etc.) is handled by the logout page.
     * 
     * If no navigation callback is set, this method does nothing.
     */
    private void logout() {
        if (navigate != null) {
            navigate.accept("/logout");
        }
    }

    /**
     * Displays a modal alert dialog to the user.
     * 
     * This is a utility method used throughout the class to show
     * feedback messages (errors, success, information, etc.) to the user.
     * 
     * The alert blocks execution until the user closes it (showAndWait).
     * The header is set to null for a cleaner, simpler appearance.
     * 
     * @param title   The title of the alert dialog window
     * @param message The main message content to display to the user
     * @param type    The type of alert (ERROR, INFORMATION, WARNING, CONFIRMATION)
     *                which determines the icon and styling
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Applies theme-aware styling to a button with hover effects.
     * 
     * This method creates a consistent button appearance that matches
     * the application's current theme. The button changes color when
     * the mouse hovers over it for better user interaction feedback.
     * 
     * Styling includes:
     * - Background color from theme (changes on hover)
     * - White text color
     * - Bold 14px font
     * - Padding of 12px vertical, 30px horizontal
     * - Rounded corners (8px radius)
     * - Hand cursor on hover
     * 
     * @param button The button to apply styling to
     * @param theme  The ThemeManager instance that provides color values
     */
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

    /**
     * Applies theme-aware styling to the logout button.
     * 
     * This is a convenience wrapper method that delegates to applyThemeToButton.
     * It exists to maintain consistency in method naming and allows for
     * potential future customization of logout button styling if needed.
     * 
     * Currently, the logout button uses the same theme colors as other
     * action buttons.
     * 
     * @param button The logout button to apply styling to
     * @param theme  The ThemeManager instance that provides color values
     */
    private void applyThemeToLogoutButton(Button button, ThemeManager theme) {
        // Use theme color for logout button too
        applyThemeToButton(button, theme);
    }

    /**
     * Applies red "danger" styling to the delete account button.
     * 
     * Unlike other buttons which use theme colors, the delete button
     * always uses red (#e74c3c) to visually indicate that it performs
     * a dangerous, destructive action (account deletion).
     * 
     * The button has similar styling to other buttons but with:
     * - Red normal color (#e74c3c)
     * - Darker red hover color (#c0392b)
     * - Same text, font, padding, and border radius as theme buttons
     * 
     * This color choice follows UI/UX best practices for destructive actions,
     * making it clear to users that this action is permanent and irreversible.
     * 
     * @param button The delete button to apply red danger styling to
     */
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
