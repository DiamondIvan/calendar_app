package com.example.backend.utils;

/**
 * AppTheme enum defines complete color schemes for the application (backend).
 * 
 * Each theme includes a coordinated set of colors:
 * - User selected color: Main theme accent color
 * - Button color: Primary action button color
 * - Hover button color: Darker shade for hover states
 * - Background color: Solid background color
 * - Background gradient: CSS gradient for backgrounds
 * 
 * Available themes:
 * - BLUE: Cool blue tones (default)
 * - ORANGE: Warm orange tones
 * - PINK: Soft pink tones
 * - GREEN: Natural green tones
 * - PURPLE: Rich purple tones
 * - RED: Bold red tones
 * - LIGHTBLUE: Sky blue tones
 * - WHITE: Neutral gray/white tones
 * - YELLOW: Bright yellow tones
 */
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

    /**
     * Constructs an AppTheme with a complete color scheme.
     * 
     * @param userSelectedColor  Main accent color (hex code)
     * @param buttonColor        Primary button color (hex code)
     * @param hoverButtonColor   Button hover state color (hex code)
     * @param backgroundColor    Solid background color (hex code)
     * @param backgroundGradient CSS gradient string for backgrounds
     */
    AppTheme(String userSelectedColor, String buttonColor, String hoverButtonColor, String backgroundColor,
            String backgroundGradient) {
        this.userSelectedColor = userSelectedColor;
        this.buttonColor = buttonColor;
        this.hoverButtonColor = hoverButtonColor;
        this.backgroundColor = backgroundColor;
        this.backgroundGradient = backgroundGradient;
    }

    /**
     * Returns the main accent color for this theme.
     * 
     * @return The user selected color as a hex code (e.g., "#90caf9")
     */
    public String getUserSelectedColor() {
        return userSelectedColor;
    }

    /**
     * Returns the primary button color for this theme.
     * 
     * @return The button color as a hex code (e.g., "#4285f4")
     */
    public String getButtonColor() {
        return buttonColor;
    }

    /**
     * Returns the button hover state color for this theme.
     * 
     * @return The hover button color as a hex code (darker shade of button color)
     */
    public String getHoverButtonColor() {
        return hoverButtonColor;
    }

    /**
     * Returns the solid background color for this theme.
     * 
     * @return The background color as a hex code (e.g., "#c9d6ff")
     */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Returns the CSS gradient string for backgrounds.
     * 
     * @return The background gradient as a CSS linear-gradient string
     */
    public String getBackgroundGradient() {
        return backgroundGradient;
    }
}
