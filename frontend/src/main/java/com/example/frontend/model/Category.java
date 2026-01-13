package com.example.frontend.model;

/**
 * Category enum defines event categories with associated display names and
 * colors.
 * 
 * Each category has:
 * - An ID (matches enum constant name)
 * - A user-friendly display name
 * - A hexadecimal color code for visual representation
 * 
 * Available categories:
 * - PROFESSIONAL: Work-related events (Red - #F44336)
 * - PERSONAL: Personal and lifestyle events (Orange - #FF9800)
 * - HEALTH: Health and wellness events (Yellow - #FFEB3B)
 * - EDUCATION: Educational events and learning (Green - #4CAF50)
 * - SOCIAL: Social and entertainment events (Blue - #2196F3)
 * - FINANCE: Financial events and planning (Indigo - #3F51B5)
 * - HOLIDAY: Holidays and special events (Violet - #9C27B0)
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
     * Constructs a Category with ID, display name, and color.
     * 
     * @param id       The category identifier (matches enum constant)
     * @param name     The user-friendly display name
     * @param colorHex The hexadecimal color code (e.g., "#FF0000")
     */
    Category(String id, String name, String colorHex) {
        this.id = id;
        this.name = name;
        this.colorHex = colorHex;
    }

    /**
     * Returns the user-friendly display name of this category.
     * 
     * @return The category's display name (e.g., "Professional & Work")
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns the category identifier.
     * 
     * @return The category ID (matches enum constant name)
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the user-friendly display name of this category.
     * 
     * @return The category's display name (e.g., "Professional & Work")
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the hexadecimal color code for this category.
     * 
     * @return The color code in hex format (e.g., "#F44336")
     */
    public String getColorHex() {
        return colorHex;
    }
}
