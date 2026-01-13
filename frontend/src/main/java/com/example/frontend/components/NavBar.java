package com.example.frontend.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * NavBar is a reusable navigation component that displays icon buttons for
 * navigating
 * between major sections of the application.
 * 
 * The navigation bar includes buttons for:
 * - Home
 * - Calendar
 * - Login
 * - Statistics
 * - Settings
 * 
 * Each button displays an icon image and navigates to the corresponding route
 * when clicked.
 * This component extends HBox and is styled with CSS class "nav-bar".
 */
public class NavBar extends HBox {

    private final Consumer<String> navigate;

    /**
     * Constructs a NavBar with a navigation callback.
     * Sets up the horizontal layout with icon buttons for each major section.
     * 
     * Layout specifications:
     * - 15px spacing between buttons
     * - 10px padding around the bar
     * - Left-aligned buttons
     * - Transparent background with pickOnBounds disabled to avoid blocking mouse
     * events
     * 
     * @param navigate A callback function that handles navigation to different
     *                 routes
     */
    public NavBar(Consumer<String> navigate) {
        this.navigate = navigate;

        this.setSpacing(15);
        this.setPadding(new Insets(10));
        this.setAlignment(Pos.CENTER_LEFT);
        this.getStyleClass().add("nav-bar");

        // Ensure it picks up mouse events and doesn't get blocked if overlaid
        this.setPickOnBounds(false);

        // Buttons
        Button homeBtn = createNavButton("home.png", "/");
        Button calBtn = createNavButton("calender1.png", "/calendar");
        Button loginBtn = createNavButton("Login1.png", "/login");
        Button statsBtn = createNavButton("bar-chart.png", "/statistics");
        Button settingsBtn = createNavButton("settings.png", "/settings");

        this.getChildren().addAll(homeBtn, calBtn, loginBtn, statsBtn, settingsBtn);
    }

    /**
     * Creates a navigation button with an icon image.
     * 
     * Attempts to load an icon image from the resources folder.
     * If the image cannot be loaded, falls back to a bullet point ("•") as text.
     * 
     * The button is styled with:
     * - Transparent background
     * - Hand cursor on hover
     * - 40x40 pixel icon image (preserving aspect ratio)
     * - CSS class "nav-icon-btn"
     * 
     * @param iconFileName The filename of the icon image to load (e.g., "home.png")
     *                     Images should be located in /frontend/public/
     * @param route        The navigation route to go to when the button is clicked
     *                     (e.g., "/calendar")
     * @return A Button configured with the icon and navigation action
     */
    private Button createNavButton(String iconFileName, String route) {
        Button btn = new Button();
        btn.getStyleClass().add("nav-icon-btn");
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        // Load icon image
        try {
            InputStream is = getClass().getResourceAsStream("/frontend/public/" + iconFileName);
            if (is != null) {
                Image img = new Image(is);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(40);
                iv.setFitHeight(40);
                iv.setPreserveRatio(true);
                btn.setGraphic(iv);
            }
        } catch (Exception e) {
            // Fallback to text if image not found
            btn.setText("•");
        }

        btn.setOnAction(e -> {
            if (navigate != null) {
                navigate.accept(route);
            }
        });
        return btn;
    }
}
