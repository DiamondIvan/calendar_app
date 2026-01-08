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
import com.example.frontend.model.StaticEvents;
import java.util.function.Consumer;

public class StatisticsPage {

    private final Consumer<String> navigate;

    public StatisticsPage() {
        this.navigate = null;
    }

    public StatisticsPage(Consumer<String> navigate) {
        this.navigate = navigate;
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

        for (Category cat : Category.values()) {
            long count = StaticEvents.STATIC_SUGGESTIONS.stream()
                    .filter(e -> cat.getId().equalsIgnoreCase(e.getCategory()))
                    .count();

            if (count == 0)
                count = (long) (Math.random() * 5);

            XYChart.Data<String, Number> data = new XYChart.Data<>(cat.getName(), count);
            series.getData().add(data);
        }

        barChart.getData().add(series);

        root.getChildren().addAll(title, barChart);
        return root;
    }
}
