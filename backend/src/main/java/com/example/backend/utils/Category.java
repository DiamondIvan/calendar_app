package com.example.backend.utils;

/**
 * Category enum defines event categories (backend version).
 * Identical to the frontend Category enum.
 * 
 * See frontend Category for detailed documentation.
 * Contains 7 categories (PROFESSIONAL, PERSONAL, HEALTH, EDUCATION, SOCIAL,
 * FINANCE, HOLIDAY)
 * with IDs, display names, and hex color codes.
 */
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

    /**
     * Constructs a Category with ID, name, and color.
     * 
     * @param id       Category identifier
     * @param name     Display name
     * @param colorHex Hex color code
     */
    Category(String id, String name, String colorHex) {
        this.id = id;
        this.name = name;
        this.colorHex = colorHex;
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
