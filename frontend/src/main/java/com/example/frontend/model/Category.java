package com.example.frontend.model;

public enum Category {
    PROFESSIONAL("#2196F3"),
    PERSONAL("#4CAF50"),
    HOLIDAY("#FF9800"),
    BIRTHDAY("#9C27B0"),
    OTHER("#9E9E9E");

    private final String colorHex;

    Category(String colorHex) {
        this.colorHex = colorHex;
    }

    @Override
    public String toString() {
        // Capitalize first letter, lowercase rest
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public String getId() {
        return name();
    }

    public String getName() {
        return toString();
    }

    public String getColorHex() {
        return colorHex;
    }
}
