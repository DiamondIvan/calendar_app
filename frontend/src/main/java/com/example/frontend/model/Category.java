package com.example.frontend.model;

public enum Category {
    PROFESSIONAL("PROFESSIONAL", "Professional & Work", "#F44336"), // Red
    PERSONAL("PERSONAL", "Personal & Lifestyle", "#FF9800"), // Orange
    HEALTH("HEALTH", "Health", "#FFEB3B"), // Yellow
    EDUCATION("EDUCATION", "Education", "#4CAF50"), // Green
    SOCIAL("SOCIAL", "Social & Entertainment", "#2196F3"), // Blue
    FINANCE("FINANCE", "Finance", "#3F51B5"), // Indigo
    HOLIDAY("HOLIDAY", "Holiday & Events", "#9C27B0"); // Violet

    private final String id;
    private final String name;
    private final String colorHex;

    Category(String id, String name, String colorHex) {
        this.id = id;
        this.name = name;
        this.colorHex = colorHex;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColorHex() {
        return colorHex;
    }
}
