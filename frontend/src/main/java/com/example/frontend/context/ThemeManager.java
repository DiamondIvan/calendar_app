package com.example.frontend.context;

import com.example.frontend.model.AppTheme;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Region;

public class ThemeManager {
    private static final ThemeManager instance = new ThemeManager();
    private final ObjectProperty<AppTheme> currentTheme = new SimpleObjectProperty<>(AppTheme.BLUE);

    // CalendarPage theme scheme (persisted across navigation for the session)
    private final StringProperty userSelectedColor = new SimpleStringProperty("#90caf9");
    private final StringProperty buttonColor = new SimpleStringProperty("#4285f4");
    private final StringProperty hoverButtonColor = new SimpleStringProperty("#2f6fe0");
    private final StringProperty backgroundGradient = new SimpleStringProperty(
            "linear-gradient(to right, #e2e2e2, #c9d6ff)");

    private ThemeManager() {
    }

    public static ThemeManager getInstance() {
        return instance;
    }

    public ObjectProperty<AppTheme> currentThemeProperty() {
        return currentTheme;
    }

    public AppTheme getCurrentTheme() {
        return currentTheme.get();
    }

    public void setCurrentTheme(AppTheme theme) {
        this.currentTheme.set(theme);
        // Logic to apply styles globally could go here or listeners can be attached in
        // views
    }

    public String getUserSelectedColor() {
        return userSelectedColor.get();
    }

    public String getButtonColor() {
        return buttonColor.get();
    }

    public String getHoverButtonColor() {
        return hoverButtonColor.get();
    }

    public String getBackgroundGradient() {
        return backgroundGradient.get();
    }

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
