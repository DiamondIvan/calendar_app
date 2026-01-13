package com.example.frontend.pages;

import com.example.frontend.context.ThemeManager;
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

/**
 * CreateEventPage provides a comprehensive UI for creating calendar events.
 * 
 * This page allows users to create both single and recurring events with:
 * - Event title and description
 * - Category selection
 * - Start and end date/time selection
 * - Optional recurring event settings (daily, weekly, monthly)
 * - Recurrence end date and occurrence count
 * 
 * Features:
 * - Login requirement enforcement (redirects to login if not authenticated)
 * - Navigation bar integration
 * - Form validation for required fields
 * - Time slot selection in 15-minute intervals
 * - Automatic event saving to CSV via EventCsvService
 * - Theme integration for consistent styling
 */
public class CreateEventPage {

    /** Service for managing event data and CSV operations */
    private final EventCsvService eventService;

    /** Currently logged-in user (required for event creation) */
    private final AppUser currentUser;

    /** Input field for event title */
    private TextField titleField;

    /** Text area for event description */
    private TextArea descArea;

    /** Dropdown for selecting event category */
    private ComboBox<Category> categoryBox;

    /** Date picker for event start date */
    private DatePicker startDatePicker;

    /** Time selector for event start time */
    private ComboBox<String> startTimeBox;

    /** Date picker for event end date */
    private DatePicker endDatePicker;

    /** Time selector for event end time */
    private ComboBox<String> endTimeBox;

    /**
     * Dropdown for selecting recurrence interval (None, Daily, Weekly, Monthly,
     * Yearly)
     */
    private ComboBox<String> recurrentIntervalBox;

    /** Spinner for setting number of recurrence occurrences */
    private Spinner<Integer> recurrentTimesSpinner;

    /** Date picker for recurrence end date */
    private DatePicker recurrentEndDatePicker;

    /** Navigation callback for routing to other pages */
    private Consumer<String> navigate;

    /**
     * Constructor with event service and current user.
     * 
     * @param eventService Service for managing events
     * @param currentUser  Currently logged-in user (required for creating events)
     */
    public CreateEventPage(EventCsvService eventService, AppUser currentUser) {
        this.eventService = eventService;
        this.currentUser = currentUser;
    }

    /**
     * Full constructor with navigation callback.
     * 
     * @param eventService Service for managing events
     * @param currentUser  Currently logged-in user (required for creating events)
     * @param navigate     Navigation callback for routing between pages
     */
    public CreateEventPage(EventCsvService eventService, AppUser currentUser, Consumer<String> navigate) {
        this.eventService = eventService;
        this.currentUser = currentUser;
        this.navigate = navigate;
    }

    /**
     * Default constructor for testing.
     * Creates a test user and new event service.
     */
    public CreateEventPage() {
        this(new EventCsvService(), new AppUser(1, "Test", "test@test.com", "pass"));
    }

    /**
     * Builds and returns the create event page UI.
     * 
     * If the user is not logged in (null or invalid ID), redirects to a
     * login-required page.
     * 
     * The page includes:
     * - Navigation bar (if navigate callback is provided)
     * - Back button to return to calendar
     * - Form fields for event details:
     * - Title (required)
     * - Description
     * - Category selection
     * - Start and end date/time
     * - Optional recurrence settings:
     * - Recurrence interval (Daily, Weekly, Monthly, Yearly)
     * - Number of occurrences
     * - End date for recurrence
     * - Create button to submit the form
     * 
     * The form validates required fields and time logic before creating events.
     * Recurring events generate multiple event instances based on the interval.
     * 
     * @return A ScrollPane containing the complete create event form
     */
    public Node getView() {
        // Check if user is logged in - redirect to login if not
        if (currentUser == null || currentUser.getId() == null || currentUser.getId() <= 0) {
            return new LoginRequiredPage(navigate, "Please log in to create events", "/login", "Go to Login")
                    .getView();
        }

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

        ThemeManager theme = ThemeManager.getInstance();
        container.setStyle("-create-accent-color: " + theme.getButtonColor() + "; " +
                "-create-accent-hover-color: " + theme.getHoverButtonColor() + ";");

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

        recurrentTimesSpinner = new Spinner<>(1, 100, 5);
        recurrentTimesSpinner.setEditable(true);
        recurrentTimesSpinner.setMaxWidth(Double.MAX_VALUE);
        // recurrentTimesSpinner.getStyleClass().add("spinner"); // Optional custom
        // style

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
                recTimesLbl, recurrentTimesSpinner,
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

    /**
     * Handles form submission and event creation.
     * 
     * Validation steps:
     * 1. Ensures title, start date, and end date are provided
     * 2. Parses start and end times (defaults to 00:00 and 23:59 if not set)
     * 3. Validates that end time is after start time
     * 4. Verifies user is logged in
     * 
     * Event creation:
     * - Creates Event object with user ID, title, description, dates, and category
     * - If recurrence is set (not "None"):
     * - Converts UI interval to code (e.g., "Daily" → "1d")
     * - Generates multiple recurring event instances
     * - If no recurrence, saves single event
     * 
     * On success:
     * - Shows success alert
     * - Clears form fields
     * 
     * On error:
     * - Shows error alert with exception details
     */
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
            // Set user ID (should always have currentUser due to login check)
            if (currentUser != null && currentUser.getId() != null) {
                event.setUserId(currentUser.getId());
                System.out.println("DEBUG CreateEventPage: Set userId to " + currentUser.getId() + " for user "
                        + currentUser.getName());
            } else {
                // This shouldn't happen due to login check, but fallback just in case
                System.err.println("ERROR: currentUser is null or has null ID!");
                showAlert("Error", "You must be logged in to create events");
                return;
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
                event.setRecurrentTimes(String.valueOf(recurrentTimesSpinner.getValue()));
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

    /**
     * Displays an information alert dialog.
     * 
     * @param header  The alert header text
     * @param content The alert content/message text
     */
    private void showAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Generates a list of time slots in 15-minute intervals.
     * 
     * Creates time slots from 00:00 to 23:45 in HH:mm format.
     * Each hour has 4 slots (00, 15, 30, 45 minutes).
     * 
     * @return A list of time strings (e.g., "09:00", "09:15", "09:30", "09:45")
     */
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
