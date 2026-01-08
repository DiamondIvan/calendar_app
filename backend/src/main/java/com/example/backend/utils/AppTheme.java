package com.example.backend.utils;

public enum AppTheme {
    BLUE("#90caf9", "#4285f4", "#2f6fe0", "#c9d6ff", "linear-gradient(to right, #e2e2e2, #c9d6ff)"),
    ORANGE("#ffcc80", "#fb8c00", "#ef6c00", "#fff3e0", "linear-gradient(to right, #e2e2e2, #ffe0b2)"),
    PINK("#f8bbd0", "#ec407a", "#d81b60", "#fce4ec", "linear-gradient(to right, #e2e2e2, #f8bbd0)"),
    GREEN("#c8e6c9", "#43a047", "#2e7d32", "#e8f5e9", "linear-gradient(to right, #e2e2e2, #c8e6c9)"),
    PURPLE("#d1c4e9", "#7e57c2", "#5e35b1", "#ede7f6", "linear-gradient(to right, #e2e2e2, #d1c4e9)"),
    RED("#ef9a9a", "#e53935", "#c62828", "#ffebee", "linear-gradient(to right, #e2e2e2, #ef9a9a)"),
    LIGHTBLUE("#b3e5fc", "#039be5", "#0277bd", "#e1f5fe", "linear-gradient(to right, #e2e2e2, #b3e5fc)"),
    WHITE("#ffffff", "#616161", "#424242", "#f5f5f5", "linear-gradient(to right, #e2e2e2, #f5f5f5)"),
    YELLOW("#fff59d", "#fbc02d", "#f9a825", "#fffde7", "linear-gradient(to right, #e2e2e2, #fff59d)");

    private final String userSelectedColor;
    private final String buttonColor;
    private final String hoverButtonColor;
    private final String backgroundColor;
    private final String backgroundGradient;

    AppTheme(String userSelectedColor, String buttonColor, String hoverButtonColor, String backgroundColor,
            String backgroundGradient) {
        this.userSelectedColor = userSelectedColor;
        this.buttonColor = buttonColor;
        this.hoverButtonColor = hoverButtonColor;
        this.backgroundColor = backgroundColor;
        this.backgroundGradient = backgroundGradient;
    }

    public String getUserSelectedColor() {
        return userSelectedColor;
    }

    public String getButtonColor() {
        return buttonColor;
    }

    public String getHoverButtonColor() {
        return hoverButtonColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getBackgroundGradient() {
        return backgroundGradient;
    }
}
