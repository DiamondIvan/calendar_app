package com.example.frontend.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalTime;
import java.time.LocalDateTime;
import com.example.frontend.model.Category;
import com.example.frontend.service.EventCsvService;
import com.example.frontend.model.AppUser;
import com.example.frontend.model.Event;

import java.util.function.Consumer;

public class CreateEventPage {

    private final EventCsvService eventService;
    private final AppUser currentUser;

    private TextField titleField;
    private TextArea descArea;
    private ComboBox<Category> categoryBox;
    private DatePicker startDatePicker;
    private ComboBox<String> startTimeBox;
    private DatePicker endDatePicker;
    private ComboBox<String> endTimeBox;

    // Recurrent
    private ComboBox<String> recurrentIntervalBox;
    private TextField recurrentTimesField;
    private DatePicker recurrentEndDatePicker;

    private Consumer<String> navigate;

    public CreateEventPage(EventCsvService eventService, AppUser currentUser) {
        this.eventService = eventService;
        this.currentUser = currentUser;
    }

    public CreateEventPage(EventCsvService eventService, AppUser currentUser, Consumer<String> navigate) {
        this.eventService = eventService;
        this.currentUser = currentUser;
        this.navigate = navigate;
    }

    public CreateEventPage() {
        this(new EventCsvService(), new AppUser(1, "Test", "test@test.com", "pass"));
    }

    public Node getView() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox container = new VBox();

        // NavBar
        if (navigate != null) {
            container.getChildren().add(new com.example.frontend.components.NavBar(navigate));
        }

        container.getStyleClass().add("event-container");
        container.setAlignment(Pos.TOP_CENTER);
        // Center the container in the scrollpane if wide enough
        // usually managed by parenting layout, but let's try to constrain width
        container.setMaxWidth(750);

        try {
            container.getStylesheets()
                    .add(getClass().getResource("/frontend/CSS_SubPage/CreateEventPage.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS Error");
        }

        // Header Box (Back Button + Title)
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        // Back Button
        Button backBtn = new Button("←");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> {
            if (navigate != null)
                navigate.accept("/calendar");
        });

        Label titleLabel = new Label("Create Event");
        titleLabel.getStyleClass().add("event-title");

        headerBox.getChildren().addAll(backBtn, titleLabel);

        VBox form = new VBox(10);
        form.setAlignment(Pos.TOP_LEFT);
        form.setFillWidth(true);

        // Title
        titleField = new TextField();
        titleField.setPromptText("Event Title");
        titleField.getStyleClass().add("text-field");

        // Description
        descArea = new TextArea();
        descArea.setPromptText("Event Description");
        descArea.setPrefRowCount(4);
        descArea.getStyleClass().add("text-area");

        // Category
        Label catLbl = new Label("Event Category");
        catLbl.getStyleClass().add("event-label");

        categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(Category.values());
        categoryBox.setPromptText("Select Category");
        categoryBox.setMaxWidth(Double.MAX_VALUE); // Fill width
        categoryBox.getStyleClass().add("combo-box");

        // Start Date/Time
        Label startLbl = new Label("Start Date & Time");
        startLbl.getStyleClass().add("event-label");

        HBox startBox = new HBox(10);
        startDatePicker = new DatePicker();
        startDatePicker.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(startDatePicker, Priority.ALWAYS);

        startTimeBox = new ComboBox<>();
        startTimeBox.getItems().addAll(generateTimeSlots());
        startTimeBox.setPromptText("HH:mm");
        startTimeBox.setPrefWidth(120);
        startTimeBox.getStyleClass().add("combo-box");

        startBox.getChildren().addAll(startDatePicker, startTimeBox);

        // End Date/Time
        Label endLbl = new Label("End Date & Time");
        endLbl.getStyleClass().add("event-label");

        HBox endBox = new HBox(10);
        endDatePicker = new DatePicker();
        endDatePicker.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(endDatePicker, Priority.ALWAYS);

        endTimeBox = new ComboBox<>();
        endTimeBox.getItems().addAll(generateTimeSlots());
        endTimeBox.setPromptText("HH:mm");
        endTimeBox.setPrefWidth(120);
        endTimeBox.getStyleClass().add("combo-box");

        endBox.getChildren().addAll(endDatePicker, endTimeBox);

        // Divider
        Separator sep = new Separator();
        sep.setPadding(new Insets(20, 0, 20, 0));

        // Recurrent Section
        Label recTitle = new Label("Recurrent Settings (Optional)");
        recTitle.getStyleClass().add("section-title");

        Label recIntLbl = new Label("Recurrence Interval");
        recIntLbl.getStyleClass().add("event-label");

        recurrentIntervalBox = new ComboBox<>();
        recurrentIntervalBox.getItems().addAll("None", "Daily", "Weekly", "Monthly", "Yearly");
        recurrentIntervalBox.setValue("None");
        recurrentIntervalBox.setMaxWidth(Double.MAX_VALUE);
        recurrentIntervalBox.getStyleClass().add("combo-box");

        Label recTimesLbl = new Label("Number of Occurrences");
        recTimesLbl.getStyleClass().add("event-label");

        recurrentTimesField = new TextField();
        recurrentTimesField.setPromptText("e.g. 5");
        recurrentTimesField.getStyleClass().add("text-field");

        Label recEndLbl = new Label("Recurrent End Date");
        recEndLbl.getStyleClass().add("event-label");

        recurrentEndDatePicker = new DatePicker();
        recurrentEndDatePicker.setMaxWidth(Double.MAX_VALUE);
        recurrentEndDatePicker.getStyleClass().add("date-picker");

        // Submit
        Button submitBtn = new Button("CREATE EVENT");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.getStyleClass().add("button");
        submitBtn.setOnAction(e -> handleSubmit());
        VBox.setMargin(submitBtn, new Insets(20, 0, 0, 0));

        form.getChildren().addAll(
                titleField,
                descArea,
                catLbl, categoryBox,
                startLbl, startBox,
                endLbl, endBox,
                sep,
                recTitle,
                recIntLbl, recurrentIntervalBox,
                recTimesLbl, recurrentTimesField,
                recEndLbl, recurrentEndDatePicker,
                submitBtn);

        container.getChildren().addAll(headerBox, form);

        // Wrap in a VBox to center it horizontally if scrollpane is wide
        VBox wrapper = new VBox(container);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setPadding(new Insets(20));

        scrollPane.setContent(wrapper);
        return scrollPane;
    }

    private void handleSubmit() {
        try {
            // Validation
            if (titleField.getText().isEmpty() || startDatePicker.getValue() == null
                    || endDatePicker.getValue() == null) {
                showAlert("Validation Error", "Please fill in Title, Start Date, and End Date.");
                return;
            }

            // Time handling
            LocalTime startTime = (startTimeBox.getValue() == null) ? LocalTime.of(0, 0)
                    : LocalTime.parse(startTimeBox.getValue());
            LocalTime endTime = (endTimeBox.getValue() == null) ? LocalTime.of(23, 59)
                    : LocalTime.parse(endTimeBox.getValue());

            LocalDateTime start = LocalDateTime.of(startDatePicker.getValue(), startTime);
            LocalDateTime end = LocalDateTime.of(endDatePicker.getValue(), endTime);

            if (end.isBefore(start)) {
                showAlert("Validation Error", "End date must be after start date.");
                return;
            }

            Event event = new Event();
            // Handle anonymous user
            if (currentUser != null) {
                event.setUserId(currentUser.getId());
            } else {
                event.setUserId(-1); // Guest ID or handle otherwise
            }
            event.setTitle(titleField.getText());
            event.setDescription(descArea.getText());
            event.setStartDateTime(start);
            event.setEndDateTime(end);

            if (categoryBox.getValue() != null) {
                event.setCategory(categoryBox.getValue().getId()); // Use string ID
            }

            // Recurrence
            String intervalUI = recurrentIntervalBox.getValue();
            if (intervalUI != null && !intervalUI.equals("None")) {
                String code = switch (intervalUI) {
                    case "Daily" -> "1d";
                    case "Weekly" -> "1w";
                    case "Monthly" -> "1m";
                    default -> "";
                };
                event.setRecurrentInterval(code);
                event.setRecurrentTimes(recurrentTimesField.getText());
                if (recurrentEndDatePicker.getValue() != null) {
                    event.setRecurrentEndDate(recurrentEndDatePicker.getValue().toString());
                }

                // Use specialized method for recurrence
                eventService.generateAndSaveRecurringEvents(event);
            } else {
                // Single event
                eventService.saveEvent(event);
            }

            showAlert("Success", "Event Created Successfully!");

            // Clear fields (optional)
            titleField.clear();
            descArea.clear();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to create event: " + e.getMessage());
        }
    }

    private void showAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private java.util.List<String> generateTimeSlots() {
        java.util.List<String> times = new java.util.ArrayList<>();
        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m += 15) {
                times.add(String.format("%02d:%02d", h, m));
            }
        }
        return times;
    }
}
