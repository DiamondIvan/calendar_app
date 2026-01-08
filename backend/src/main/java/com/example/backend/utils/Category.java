package com.example.backend.utils;

public enum Category {
    PROFESSIONAL("PROFESSIONAL", "Professional", "#795548"), // Brown-ish/Forma
    PERSONAL("PERSONAL", "Personal", "#4CAF50"), // Green
    HEALTH("HEALTH", "Health", "#E91E63"), // Pink
    EDUCATION("EDUCATION", "Education", "#9C27B0"), // Purple
    SOCIAL("SOCIAL", "Social", "#FF9800"), // Orange
    FINANCE("FINANCE", "Finance", "#607D8B"), // Blue Grey
    HOLIDAY("HOLIDAY", "Holiday", "#F44336"), // Red
    OTHER("OTHER", "Other", "#9E9E9E"); // Grey

    private final String id;
    private final String name;
    private final String colorHex;

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
