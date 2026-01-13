package com.example.frontend.context;

import com.example.frontend.model.AppTheme;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Region;

/**
 * ThemeManager is a singleton class that manages application-wide theme
 * settings and colors.
 * 
 * Key features:
 * - Singleton pattern for global access
 * - Stores current theme (AppTheme enum)
 * - Manages color scheme for calendar and UI elements
 * - Provides JavaFX observable properties for reactive UI updates
 * - Persists user-selected colors across navigation during the session
 * - Maps calendar sidebar colors to corresponding login page color schemes
 * 
 * The manager maintains colors for:
 * - User selected color (calendar sidebar selection)
 * - Button color (primary action buttons)
 * - Hover button color (button hover state)
 * - Background gradient (page backgrounds)
 */
public class ThemeManager {
    private static final ThemeManager instance = new ThemeManager();
    private final ObjectProperty<AppTheme> currentTheme = new SimpleObjectProperty<>(AppTheme.BLUE);

    // CalendarPage theme scheme (persisted across navigation for the session)
    private final StringProperty userSelectedColor = new SimpleStringProperty("#90caf9");
    private final StringProperty buttonColor = new SimpleStringProperty("#4285f4");
    private final StringProperty hoverButtonColor = new SimpleStringProperty("#2f6fe0");
    private final StringProperty backgroundGradient = new SimpleStringProperty(
            "linear-gradient(to right, #e2e2e2, #c9d6ff)");

    /**
     * Private constructor to enforce singleton pattern.
     */
    private ThemeManager() {
    }

    /**
     * Returns the singleton instance of ThemeManager.
     * 
     * @return The single ThemeManager instance for the application
     */
    public static ThemeManager getInstance() {
        return instance;
    }

    /**
     * Returns the JavaFX observable property for the current theme.
     * Views can bind to this property to automatically update when theme changes.
     * 
     * @return ObjectProperty containing the current AppTheme
     */
    public ObjectProperty<AppTheme> currentThemeProperty() {
        return currentTheme;
    }

    /**
     * Gets the current theme.
     * 
     * @return The current AppTheme (e.g., BLUE, ORANGE, PINK, etc.)
     */
    public AppTheme getCurrentTheme() {
        return currentTheme.get();
    }

    /**
     * Sets the current theme and triggers any registered listeners.
     * This can be used to apply theme-wide styling changes across the application.
     * 
     * @param theme The new AppTheme to apply
     */
    public void setCurrentTheme(AppTheme theme) {
        this.currentTheme.set(theme);
        // Logic to apply styles globally could go here or listeners can be attached in
        // views
    }

    /**
     * Gets the user-selected color from the calendar sidebar.
     * This color is persisted across navigation for the session.
     * 
     * @return Hex color string (e.g., "#90caf9")
     */
    public String getUserSelectedColor() {
        return userSelectedColor.get();
    }

    /**
     * Gets the primary button color.
     * 
     * @return Hex color string for buttons (e.g., "#4285f4")
     */
    public String getButtonColor() {
        return buttonColor.get();
    }

    /**
     * Gets the button hover color.
     * 
     * @return Hex color string for button hover state (e.g., "#2f6fe0")
     */
    public String getHoverButtonColor() {
        return hoverButtonColor.get();
    }

    /**
     * Gets the background gradient CSS string.
     * 
     * @return CSS gradient string (e.g., "linear-gradient(to right, #e2e2e2,
     *         #c9d6ff)")
     */
    public String getBackgroundGradient() {
        return backgroundGradient.get();
    }

    /**
     * Updates the entire color scheme with new values.
     * Only non-null parameters will update their respective colors.
     * This allows partial updates without affecting other color properties.
     * 
     * @param userSelectedColor  New user-selected color (or null to keep current)
     * @param buttonColor        New button color (or null to keep current)
     * @param hoverButtonColor   New hover button color (or null to keep current)
     * @param backgroundGradient New background gradient (or null to keep current)
     */
    public void setScheme(String userSelectedColor, String buttonColor, String hoverButtonColor,
            String backgroundGradient) {
        if (userSelectedColor != null)
            this.userSelectedColor.set(userSelectedColor);
        if (buttonColor != null)
            this.buttonColor.set(buttonColor);
        if (hoverButtonColor != null)
            this.hoverButtonColor.set(hoverButtonColor);
        if (backgroundGradient != null)
            this.backgroundGradient.set(backgroundGradient);
    }

    /**
     * Applies the current background gradient to a JavaFX Region.
     * 
     * This method intelligently updates the -fx-background-color style:
     * - If no existing style, sets the background gradient
     * - If existing style has background-color, replaces it with the gradient
     * - If existing style has no background-color, appends the gradient
     * 
     * This preserves other existing styles while updating only the background.
     * 
     * @param region The JavaFX Region to apply the background to (e.g., VBox, HBox,
     *               Pane)
     *               If null, method returns immediately without action.
     */
    public void applyBackground(Region region) {
        if (region == null) {
            return;
        }

        String fillStyle = "-fx-background-color: " + getBackgroundGradient() + ";";
        String existing = region.getStyle();
        if (existing == null || existing.isBlank()) {
            region.setStyle(fillStyle);
            return;
        }

        if (existing.contains("-fx-background-color")) {
            region.setStyle(existing.replaceAll("-fx-background-color\\s*:\\s*[^;]+;", fillStyle));
        } else {
            region.setStyle(existing + " " + fillStyle);
        }
    }

    /**
     * Returns the LoginPage primary/secondary colors for the current Calendar
     * sidebar selection.
     * Index 0 = primary, index 1 = secondary.
     */
    public String[] getLoginPageColors() {
        String key = getUserSelectedColor();
        if (key == null) {
            return new String[] { getButtonColor(), getHoverButtonColor() };
        }

        String normalized = key.trim().toLowerCase();
        return switch (normalized) {
            case "#90caf9" -> new String[] { "#5c6bc0", "#512da8" };
            case "#ffcc80" -> new String[] { "#fb8c00", "#ef6c00" };
            case "#f8bbd0" -> new String[] { "#ec407a", "#ad1457" };
            case "#c8e6c9" -> new String[] { "#43a047", "#1b5e20" };
            case "#d1c4e9" -> new String[] { "#7e57c2", "#4527a0" };
            case "#ef9a9a" -> new String[] { "#e53935", "#b71c1c" };
            case "#b3e5fc" -> new String[] { "#039be5", "#01579b" };
            case "#ffffff" -> new String[] { "#607d8b", "#37474f" };
            default -> new String[] { getButtonColor(), getHoverButtonColor() };
        };
    }
}
