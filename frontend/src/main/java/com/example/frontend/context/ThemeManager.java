package com.example.frontend.context;

import com.example.frontend.model.AppTheme;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ThemeManager {
    private static final ThemeManager instance = new ThemeManager();
    private final ObjectProperty<AppTheme> currentTheme = new SimpleObjectProperty<>(AppTheme.BLUE);

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
}
