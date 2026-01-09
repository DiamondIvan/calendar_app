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

public class StatisticsPage {

    private final Consumer<String> navigate;
    private final EventCsvService eventService;
    private final AppUser currentUser;

    public StatisticsPage() {
        this(null, new EventCsvService(), null);
    }

    public StatisticsPage(Consumer<String> navigate) {
        this(navigate, new EventCsvService(), null);
    }

    public StatisticsPage(Consumer<String> navigate, EventCsvService eventService, AppUser currentUser) {
        this.navigate = navigate;
        this.eventService = (eventService != null) ? eventService : new EventCsvService();
        this.currentUser = currentUser;
    }

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
