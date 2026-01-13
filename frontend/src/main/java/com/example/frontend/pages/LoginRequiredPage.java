package com.example.frontend.pages;

import com.example.frontend.context.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * A reusable page that displays a message when a user tries to access a
 * protected resource
 * without being logged in. Shows a custom message and an action button to
 * navigate to login.
 * 
 * This page is used throughout the application to handle unauthenticated access
 * attempts
 * with a consistent, user-friendly interface.
 */
public class LoginRequiredPage {

    private final Consumer<String> navigate;
    private final String messageText;
    private final String actionRoute;
    private final String actionLabel;

    /**
     * Default constructor with standard login prompt.
     * Creates a page with the default message "Please log in to continue."
     * and a button that navigates to the login page.
     * 
     * @param navigate A callback function for navigation between pages
     */
    public LoginRequiredPage(Consumer<String> navigate) {
        this(navigate, "Please log in to continue.", "/login", "Go to Login");
    }

    /**
     * Full constructor with customizable message and action button.
     * Allows complete customization of the message and the action button's label
     * and route.
     * Provides default fallback values if any parameter is null or blank.
     * 
     * @param navigate    A callback function for navigation between pages
     * @param messageText The message to display to the user. Defaults to "Please
     *                    log in to continue." if null/blank.
     * @param actionRoute The route to navigate to when the action button is
     *                    clicked. Defaults to "/login" if null/blank.
     * @param actionLabel The text label for the action button. Defaults to "Go to
     *                    Login" if null/blank.
     */
    public LoginRequiredPage(Consumer<String> navigate, String messageText, String actionRoute, String actionLabel) {
        this.navigate = navigate;
        this.messageText = (messageText != null && !messageText.isBlank()) ? messageText : "Please log in to continue.";
        this.actionRoute = (actionRoute != null && !actionRoute.isBlank()) ? actionRoute : "/login";
        this.actionLabel = (actionLabel != null && !actionLabel.isBlank()) ? actionLabel : "Go to Login";
    }

    /**
     * Creates and returns the visual representation of the login required page.
     * 
     * The view includes:
     * - A centered white card with shadow effect
     * - Navigation bar (if navigate callback is provided)
     * - Custom message text explaining why login is required
     * - Themed action button that navigates to the specified route
     * - Theme-aware background and button colors
     * 
     * The layout is responsive and centered in the available space.
     * 
     * @return A JavaFX Node containing the complete login required page UI
     */
    public Node getView() {
        ThemeManager theme = ThemeManager.getInstance();

        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(30));
        theme.applyBackground(root);

        VBox content = new VBox(16);
        content.setAlignment(Pos.CENTER);

        Label message = new Label(messageText);
        message.setStyle("-fx-font-size: 18px; -fx-text-fill: #555;");

        Button actionBtn = new Button(actionLabel);
        actionBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 30;");
        themeSolidButton(actionBtn, theme.getButtonColor(), theme.getHoverButtonColor());
        actionBtn.setOnAction(e -> {
            if (navigate != null) {
                navigate.accept(actionRoute);
            }
        });

        content.getChildren().addAll(message, actionBtn);

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35));
        card.setMaxWidth(650);
        card.setStyle(
                "-fx-background-color: white;" +
                        " -fx-background-radius: 25;" +
                        " -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.18), 18, 0, 0, 6);");

        if (navigate != null) {
            com.example.frontend.components.NavBar navBar = new com.example.frontend.components.NavBar(navigate);
            navBar.setAlignment(Pos.CENTER);
            card.getChildren().add(navBar);
        }

        card.getChildren().add(content);

        StackPane cardWrap = new StackPane(card);
        cardWrap.setAlignment(Pos.CENTER);
        VBox.setVgrow(cardWrap, javafx.scene.layout.Priority.ALWAYS);

        root.getChildren().add(cardWrap);

        return root;
    }

    /**
     * Applies theme-aware styling to a button with hover effects.
     * 
     * Sets the button's background color and text color, and adds mouse hover
     * event handlers that change the background color when hovering.
     * Uses regex to replace the background color style while preserving other
     * styles.
     * 
     * @param button   The button to apply styling to. If null, method returns
     *                 immediately.
     * @param normalBg The background color for the button's normal state
     * @param hoverBg  The background color when the mouse hovers over the button
     */
    private void themeSolidButton(Button button, String normalBg, String hoverBg) {
        if (button == null) {
            return;
        }

        button.setStyle(button.getStyle() + " -fx-background-color: " + normalBg + "; -fx-text-fill: white;");
        button.setOnMouseEntered(e -> button.setStyle(
                button.getStyle().replaceAll("-fx-background-color\\s*:[^;]+;",
                        "-fx-background-color: " + hoverBg + ";")
                        + " -fx-text-fill: white;"));
        button.setOnMouseExited(e -> button.setStyle(
                button.getStyle().replaceAll("-fx-background-color\\s*:[^;]+;",
                        "-fx-background-color: " + normalBg + ";")
                        + " -fx-text-fill: white;"));
    }
}
