package com.example.frontend.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import com.example.frontend.model.Category;
import com.example.frontend.model.Event;
import com.example.frontend.model.HolidayData;
import com.example.frontend.model.AppUser;
import com.example.frontend.service.EventCsvService;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * StatisticsPage displays event analytics and distribution by category.
 * 
 * This page provides visual statistics using a bar chart that shows:
 * - Distribution of user events across different categories
 * - Holiday events count
 * - Interactive visualization of event data
 * 
 * The page supports:
 * - Navigation bar integration
 * - User-specific event filtering
 * - Automatic inclusion of holiday data
 * - CSS styling from Statistics.css
 */
public class StatisticsPage {

    /** Navigation callback for switching between pages */
    private final Consumer<String> navigate;

    /** Service for loading and managing event data */
    private final EventCsvService eventService;

    /** Currently logged-in user (used for filtering events) */
    private final AppUser currentUser;

    /**
     * Default constructor with no navigation, new event service, and no user.
     * Used primarily for testing.
     */
    public StatisticsPage() {
        this(null, new EventCsvService(), null);
    }

    /**
     * Constructor with navigation callback only.
     * Creates a new EventCsvService and no user context.
     * 
     * @param navigate Navigation callback for page routing
     */
    public StatisticsPage(Consumer<String> navigate) {
        this(navigate, new EventCsvService(), null);
    }

    /**
     * Full constructor with all dependencies.
     * 
     * @param navigate     Navigation callback for page routing (can be null)
     * @param eventService Service for managing events (creates new if null)
     * @param currentUser  Currently logged-in user for filtering (can be null)
     */
    public StatisticsPage(Consumer<String> navigate, EventCsvService eventService, AppUser currentUser) {
        this.navigate = navigate;
        this.eventService = (eventService != null) ? eventService : new EventCsvService();
        this.currentUser = currentUser;
    }

    /**
     * Builds and returns the statistics page UI.
     * 
     * Creates a visual representation of event statistics including:
     * - Navigation bar (if navigate callback is provided)
     * - Page title
     * - Bar chart showing event distribution by category
     * - Data for both user events and holidays
     * 
     * The chart displays all category types with their respective event counts.
     * User events are filtered by the current user's ID.
     * 
     * @return A VBox containing the complete statistics page UI
     */
    public Node getView() {
        VBox root = new VBox(20);

        if (navigate != null) {
            root.getChildren().add(new com.example.frontend.components.NavBar(navigate));
        }

        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("statistics-container");
        root.setMaxWidth(1000);

        try {
            root.getStylesheets().add(getClass().getResource("/frontend/CSS_SubPage/Statistics.css").toExternalForm());
        } catch (Exception e) {
        }

        Label title = new Label("Event Statistics by Category");
        title.getStyleClass().add("stats-title");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Category");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Events");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Events Distribution");
        barChart.setLegendVisible(false);
        barChart.getStyleClass().add("chart-wrapper");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Events");

        Map<Category, Long> counts = countEventsByCategory();
        for (Category cat : Category.values()) {
            long count = counts.getOrDefault(cat, 0L);
            series.getData().add(new XYChart.Data<>(cat.getName(), count));
        }

        barChart.getData().add(series);

        root.getChildren().addAll(title, barChart);
        return root;
    }

    /**
     * Counts events by category for the current user.
     * 
     * This method:
     * 1. Initializes all categories with zero count
     * 2. Adds all holidays to the HOLIDAY category count
     * 3. Filters user events by current user ID
     * 4. Counts events in each category
     * 
     * Null events are skipped, and events are filtered to show only
     * those belonging to the current user (or none if no user logged in).
     * 
     * @return A map of categories to their event counts
     */
    private Map<Category, Long> countEventsByCategory() {
        Map<Category, Long> counts = new EnumMap<>(Category.class);
        for (Category c : Category.values()) {
            counts.put(c, 0L);
        }

        // Holidays (always visible on calendar)
        List<Event> holidays = HolidayData.getHolidays2026();
        counts.compute(Category.HOLIDAY, (k, v) -> (v == null ? 0L : v) + (long) holidays.size());

        // User events (same filter logic as CalendarPage)
        int userIdToFilter = (currentUser != null) ? currentUser.getId() : -1;
        List<Event> events = eventService.loadEvents();

        for (Event e : events) {
            if (e == null) {
                continue;
            }
            if (e.getUserId() != userIdToFilter) {
                continue;
            }

            Category resolved = resolveCategory(e.getCategory());
            counts.compute(resolved, (k, v) -> (v == null ? 0L : v) + 1L);
        }

        return counts;
    }

    /**
     * Resolves a category ID string to a Category enum.
     * 
     * Performs case-insensitive matching against category IDs.
     * If the category ID is null or doesn't match any known category,
     * defaults to PROFESSIONAL.
     * 
     * @param categoryId The category ID string to resolve (case-insensitive)
     * @return The matching Category enum, or PROFESSIONAL as default
     */
    private Category resolveCategory(String categoryId) {
        if (categoryId == null) {
            return Category.PROFESSIONAL;
        }
        String normalized = categoryId.trim().toUpperCase(Locale.ROOT);
        for (Category c : Category.values()) {
            if (c.getId().equalsIgnoreCase(normalized)) {
                return c;
            }
        }
        return Category.PROFESSIONAL;
    }
}
