package com.example.frontend.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import com.example.frontend.model.Category;
import com.example.frontend.model.HolidayData; // Import HolidayData
import com.example.frontend.service.EventCsvService;
import com.example.frontend.model.AppUser;
import com.example.frontend.model.CalendarEvent;
import com.example.frontend.model.Event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class CalendarPage {

    private final Consumer<String> navigate;
    private final EventCsvService eventService;
    private final AppUser currentUser;

    private YearMonth currentYearMonth;
    private LocalDate selectedDate; // Track selected date
    private List<CalendarEvent> events;
    private Set<String> activeFilters = new HashSet<>();

    private Label monthYearLabel;
    private GridPane calendarGrid;
    private TextField searchField;

    // Side Panel Components
    private Label sideCurrentDateNum;
    private Label sideCurrentMonth;
    private Label sideCurrentYear;
    private Rectangle sideColorBox; // Promoted to field for referencing
    private Button catBtn; // Promoted to field
    private Button createBtn; // Promoted to field
    private Button prevBtn;
    private Button nextBtn;

    // Side Panel Components

    private static String toHex(Color color) {
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private static void setInlineTextFill(Label label, Color color) {
        if (label == null) {
            return;
        }
        String existing = label.getStyle();
        String fillStyle = "-fx-text-fill: " + toHex(color) + ";";

        if (existing == null || existing.isBlank()) {
            label.setStyle(fillStyle);
            return;
        }

        if (existing.contains("-fx-text-fill")) {
            label.setStyle(existing.replaceAll("-fx-text-fill\\s*:\\s*[^;]+;", fillStyle));
        } else {
            label.setStyle(existing + " " + fillStyle);
        }
    }

    private void applyTheme(
            VBox mainContainer,
            String userSelectedColor,
            String buttonColor,
            String hoverButtonColor,
            String backgroundGradient) {
        // 1. Change Main Background with gradient
        mainContainer.setStyle("-fx-background-color: " + backgroundGradient + ";");

        // 2. Change Large Sidebar Box
        if (sideColorBox != null) {
            sideColorBox.setFill(Color.web(userSelectedColor));
        }

        // 3. Change Sidebar Text Color
        Color sidebarTextColor = Color.web(buttonColor);
        setInlineTextFill(sideCurrentDateNum, sidebarTextColor);
        setInlineTextFill(sideCurrentMonth, sidebarTextColor);
        setInlineTextFill(sideCurrentYear, sidebarTextColor);

        // 4. Change Buttons (and borders) with hover effects
        // Override the CSS `.green-btn` border by setting inline border styles.
        String outlineStyle = "-fx-background-color: white;"
                + " -fx-text-fill: " + buttonColor + ";"
                + " -fx-border-color: " + buttonColor + ";"
                + " -fx-border-width: 1px;"
                + " -fx-border-radius: 5px;"
                + " -fx-background-radius: 5px;"
                + " -fx-cursor: hand;";

        String outlineHoverStyle = "-fx-background-color: " + hoverButtonColor + ";"
                + " -fx-text-fill: white;"
                + " -fx-border-color: " + hoverButtonColor + ";"
                + " -fx-border-width: 1px;"
                + " -fx-border-radius: 5px;"
                + " -fx-background-radius: 5px;"
                + " -fx-cursor: hand;";

        String filledStyle = "-fx-background-color: " + buttonColor + ";"
                + " -fx-text-fill: white;"
                + " -fx-border-color: " + buttonColor + ";"
                + " -fx-border-width: 1px;"
                + " -fx-border-radius: 5px;"
                + " -fx-background-radius: 5px;"
                + " -fx-cursor: hand;";

        String filledHoverStyle = "-fx-background-color: " + hoverButtonColor + ";"
                + " -fx-text-fill: white;"
                + " -fx-border-color: " + hoverButtonColor + ";"
                + " -fx-border-width: 1px;"
                + " -fx-border-radius: 5px;"
                + " -fx-background-radius: 5px;"
                + " -fx-cursor: hand;";

        if (prevBtn != null) {
            prevBtn.setStyle(outlineStyle);
            prevBtn.setOnMouseEntered(evt -> prevBtn.setStyle(outlineHoverStyle));
            prevBtn.setOnMouseExited(evt -> prevBtn.setStyle(outlineStyle));
        }

        if (nextBtn != null) {
            nextBtn.setStyle(outlineStyle);
            nextBtn.setOnMouseEntered(evt -> nextBtn.setStyle(outlineHoverStyle));
            nextBtn.setOnMouseExited(evt -> nextBtn.setStyle(outlineStyle));
        }

        if (catBtn != null) {
            catBtn.setStyle(outlineStyle);
            catBtn.setOnMouseEntered(evt -> catBtn.setStyle(outlineHoverStyle));
            catBtn.setOnMouseExited(evt -> catBtn.setStyle(outlineStyle));
        }

        if (createBtn != null) {
            createBtn.setStyle(filledStyle);
            createBtn.setOnMouseEntered(evt -> createBtn.setStyle(filledHoverStyle));
            createBtn.setOnMouseExited(evt -> createBtn.setStyle(filledStyle));
        }
    }

    public CalendarPage(Consumer<String> navigate, EventCsvService eventService, AppUser currentUser) {
        this.navigate = navigate;
        this.eventService = eventService;
        this.currentUser = currentUser;

        this.currentYearMonth = YearMonth.now();
        this.selectedDate = LocalDate.now(); // Default select today
        this.events = new ArrayList<>();

        // Initialize active filters with all categories
        for (Category c : Category.values()) {
            activeFilters.add(c.getId());
        }

        loadEventsFromBackend();
    }

    public CalendarPage() {
        this(null, new EventCsvService(), null);
    }

    private void loadEventsFromBackend() {
        this.events.clear();

        // 1. Load Public Holidays (Always visible)
        List<Event> holidays = HolidayData.getHolidays2026();
        for (Event h : holidays) {
            CalendarEvent viewEvent = new CalendarEvent(
                    String.valueOf(h.getId()), // -1
                    h.getTitle(),
                    h.getStartDateTime(),
                    h.getEndDateTime(),
                    "#FF5722", // Orange for Holidays
                    "HOLIDAY");
            this.events.add(viewEvent);
        }

        // 2. Load User Events
        List<Event> dbEvents = eventService.loadEvents();
        // DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        int userIdToFilter = (currentUser != null) ? currentUser.getId() : -1;

        for (Event dbEvent : dbEvents) {
            // Show events that match the current user ID (or -1 for anonymous users)
            if (dbEvent.getUserId() != userIdToFilter)
                continue;
            try {
                LocalDateTime start = dbEvent.getStartDateTime();
                LocalDateTime end = dbEvent.getEndDateTime();
                CalendarEvent viewEvent = new CalendarEvent(
                        String.valueOf(dbEvent.getId()),
                        dbEvent.getTitle(),
                        start,
                        end,
                        ResolveCategoryColor(dbEvent.getCategory()),
                        dbEvent.getCategory().toUpperCase());
                this.events.add(viewEvent);
            } catch (Exception e) {
                System.out.println("Error parsing event");
            }
        }
    }

    // Helper to map category name to color code (since we might be using IDs)
    private String ResolveCategoryColor(String categoryId) {
        if (categoryId == null)
            return "#4CAF50";
        String cleanId = categoryId.trim();
        for (Category c : Category.values()) {
            if (c.getId().equalsIgnoreCase(cleanId))
                return c.getColorHex();
        }
        return "#4CAF50"; // Default green
    }

    public Node getView() {
        // Main Container with default gradient background
        VBox mainContainer = new VBox(20);
        mainContainer.getStyleClass().add("main-container");
        mainContainer.setPadding(new Insets(20, 40, 20, 40)); // Add padding to show background

        try {
            mainContainer.getStylesheets()
                    .add(getClass().getResource("/frontend/CSS_SubPage/CalendarPage.css").toExternalForm());
        } catch (Exception e) {
        }

        // --- NavBar (at the very top) ---
        com.example.frontend.components.NavBar navBar = new com.example.frontend.components.NavBar(navigate);

        // --- 1. Top Header Bar ---
        HBox headerBar = createHeaderBar();

        // --- 2. Content Area (Sidebar + Calendar) ---
        HBox contentArea = new HBox(30);
        contentArea.setAlignment(Pos.TOP_CENTER);

        // Left Sidebar (Card) - Pass mainContainer to control background
        VBox leftSidebar = createLeftSidebar(mainContainer);

        // Apply default theme on first load (matches first scheme)
        applyTheme(mainContainer, "#90caf9", "#4285f4", "#2f6fe0", "linear-gradient(to right, #e2e2e2, #c9d6ff)");

        // Right Calendar Area (Card)
        VBox calendarCard = new VBox();
        calendarCard.getStyleClass().add("calendar-card");
        HBox.setHgrow(calendarCard, Priority.ALWAYS);

        // Calendar Grid
        calendarGrid = new GridPane();
        calendarGrid.getStyleClass().add("calendar-grid");
        calendarGrid.setAlignment(Pos.CENTER);

        // Add scroll pane for calendar grid if needed, or just add directly
        calendarCard.getChildren().add(calendarGrid);

        contentArea.getChildren().addAll(leftSidebar, calendarCard);

        // Add all components: NavBar first, then header, then content
        mainContainer.getChildren().addAll(navBar, headerBar, contentArea);

        updateCalendar();

        return mainContainer;
    }

    private HBox createHeaderBar() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 0, 10, 0));

        // Prev Button
        prevBtn = new Button("< Prev");
        prevBtn.getStyleClass().addAll("action-btn", "green-btn");
        prevBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        // Month Label
        monthYearLabel = new Label();
        monthYearLabel.getStyleClass().add("header-month-label");

        // Next Button
        nextBtn = new Button("Next >");
        nextBtn.getStyleClass().addAll("action-btn", "green-btn");
        nextBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        // Spacer
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Search
        searchField = new TextField();
        searchField.setPromptText("Search events...");
        searchField.getStyleClass().add("search-box");
        setupSearchField();

        // Category Button
        catBtn = new Button("Category");
        catBtn.getStyleClass().addAll("action-btn", "green-btn");
        setupCategoryButton(catBtn);

        // View Icon (Mock)
        Button viewBtn = new Button("📅");
        viewBtn.getStyleClass().addAll("action-btn", "icon-btn");

        // Create Event Button
        createBtn = new Button("+ Create Event");
        createBtn.getStyleClass().addAll("action-btn", "create-btn");
        createBtn.setOnAction(e -> {
            if (navigate != null)
                navigate.accept("/create-event");
        });

        header.getChildren().addAll(prevBtn, monthYearLabel, nextBtn, spacer1, searchField, catBtn, viewBtn, createBtn);
        return header;
    }

    private VBox createLeftSidebar(VBox mainContainer) {
        VBox sidebar = new VBox(20);
        sidebar.getStyleClass().add("sidebar-card");
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPrefWidth(220);
        sidebar.setMinWidth(220);

        // Date Display
        sideCurrentDateNum = new Label(String.valueOf(LocalDate.now().getDayOfMonth()));
        sideCurrentDateNum.getStyleClass().add("sidebar-date-num");

        sideCurrentMonth = new Label(LocalDate.now().getMonth().toString()); // Initial
        sideCurrentMonth.getStyleClass().add("sidebar-month");

        sideCurrentYear = new Label(String.valueOf(LocalDate.now().getYear()));
        sideCurrentYear.getStyleClass().add("sidebar-year");

        // Large Color Box
        sideColorBox = new Rectangle(100, 100, Color.web("#90caf9")); // Default light blue
        sideColorBox.setArcWidth(20);
        sideColorBox.setArcHeight(20);

        // Background Theme Selector
        FlowPane legend = new FlowPane();
        legend.setHgap(10);
        legend.setVgap(10);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(20, 0, 0, 0));

        // Color schemes: [user-selected-color, button-color, hover-button-color,
        // background-gradient]
        String[][] colorSchemes = {
                { "#90caf9", "#4285f4", "#2f6fe0", "linear-gradient(to right, #e2e2e2, #c9d6ff)" },
                { "#ffcc80", "#fb8c00", "#ef6c00", "linear-gradient(to right, #fff3e0, #ffe0b2)" },
                { "#f8bbd0", "#ec407a", "#d81b60", "linear-gradient(to right, #fce4ec, #f8bbd0)" },
                { "#c8e6c9", "#43a047", "#2e7d32", "linear-gradient(to right, #e8f5e9, #c8e6c9)" },
                { "#d1c4e9", "#7e57c2", "#5e35b1", "linear-gradient(to right, #ede7f6, #d1c4e9)" },
                { "#ef9a9a", "#e53935", "#c62828", "linear-gradient(to right, #ffebee, #ef9a9a)" },
                { "#b3e5fc", "#039be5", "#0277bd", "linear-gradient(to right, #e1f5fe, #b3e5fc)" }
        };

        for (String[] scheme : colorSchemes) {
            String userSelectedColor = scheme[0];
            String buttonColor = scheme[1];
            String hoverButtonColor = scheme[2];
            String backgroundGradient = scheme[3];

            Rectangle r = new Rectangle(30, 30, Color.web(userSelectedColor));
            r.setArcWidth(10);
            r.setArcHeight(10);
            r.setCursor(javafx.scene.Cursor.HAND);

            // Interaction: Change Background on Click
            r.setOnMouseClicked(e -> {
                applyTheme(mainContainer, userSelectedColor, buttonColor, hoverButtonColor, backgroundGradient);
            });

            legend.getChildren().add(r);
        }

        sidebar.getChildren().addAll(sideCurrentDateNum, sideCurrentMonth, sideCurrentYear, sideColorBox, legend);
        return sidebar;
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        LocalDate firstDay = currentYearMonth.atDay(1);
        monthYearLabel.setText(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " "
                + currentYearMonth.getYear());

        // Update Sidebar
        sideCurrentMonth.setText(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        sideCurrentYear.setText(String.valueOf(currentYearMonth.getYear()));

        // --- Grid Column Config (7 columns equal width) ---
        for (int i = 0; i < 7; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / 7);
            calendarGrid.getColumnConstraints().add(colConst);
        }

        // --- Header Row (Days) ---
        // Adjust start day to Sunday to match picture
        DayOfWeek[] days = { DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY };

        for (int i = 0; i < 7; i++) {
            StackPane headerCell = new StackPane();
            headerCell.getStyleClass().add("header-cell");
            Label dayName = new Label(days[i].getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            dayName.getStyleClass().add("header-text");
            headerCell.getChildren().add(dayName);
            calendarGrid.add(headerCell, i, 0);
        }

        // --- Date Cells ---
        int col = 0;
        int row = 1;

        // Determine start offset (Sunday = 0, Monday = 1, etc.)
        // LocalDate.getDayOfWeek returns 1 (Mon) to 7 (Sun)
        int startDayVal = firstDay.getDayOfWeek().getValue(); // Mon=1
        // We want Sun=0, Mon=1...Sat=6
        // If startDayVal is 7 (Sun), offset should be 0
        // If 1 (Mon), offset 1
        int offset = (startDayVal == 7) ? 0 : startDayVal;

        // Previous Month Filler
        LocalDate prevMonth = firstDay.minusDays(offset);
        for (int i = 0; i < offset; i++) {
            VBox cell = createDayCell(prevMonth.plusDays(i), true);
            calendarGrid.add(cell, col++, row);
        }

        // Current Month
        int daysInMonth = currentYearMonth.lengthOfMonth();
        for (int i = 0; i < daysInMonth; i++) {
            LocalDate date = firstDay.plusDays(i);
            VBox cell = createDayCell(date, false);
            calendarGrid.add(cell, col++, row);

            if (col > 6) {
                col = 0;
                row++;
            }
        }

        // Next Month Filler to complete the grid (up to 6 rows usually)
        // int remainingCells = (42 - (offset + daysInMonth)); // 6 rows * 7 cols = 42
        // Just fill the rest of the current row and maybe next
        LocalDate nextMonth = firstDay.plusMonths(1);
        int addedNext = 0;
        while (row < 7) { // Ensure fixed height grid roughly
            if (col > 6) {
                col = 0;
                row++;
                if (row >= 7)
                    break;
            }
            VBox cell = createDayCell(nextMonth.plusDays(addedNext++), true);
            calendarGrid.add(cell, col++, row);

            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createDayCell(LocalDate date, boolean isOtherMonth) {
        VBox cell = new VBox(5);

        // Hide other month cells completely
        if (isOtherMonth) {
            cell.setVisible(false);
            return cell;
        }

        cell.getStyleClass().add("day-cell");

        // Selection Check (Use distinct Blue for selection to differentiate from
        // Categories)
        if (date.equals(selectedDate)) {
            cell.setStyle(
                    "-fx-background-color: #E3F2FD; -fx-border-color: #2196F3; -fx-border-width: 2px; -fx-effect: dropshadow(gaussian, rgba(33, 150, 243, 0.4), 10, 0, 0, 0);");
        }

        // Interaction: Click to update sidebar and visuals
        cell.setOnMouseClicked(e -> {
            this.selectedDate = date; // Update selection state

            // Update Sidebar
            sideCurrentDateNum.setText(String.valueOf(date.getDayOfMonth()));
            sideCurrentMonth.setText(date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            sideCurrentYear.setText(String.valueOf(date.getYear()));

            // Update Visuals (Re-render grid)
            updateCalendar();
        });

        // Today Check (Removed green background, keep text bold/different if needed, or
        // remove completely)
        // if (date.equals(LocalDate.now()) && !date.equals(selectedDate)) {
        // cell.getStyleClass().add("current-day");
        // }

        // Date Number
        Label num = new Label(String.valueOf(date.getDayOfMonth()));
        num.getStyleClass().add("day-number");
        HBox numBox = new HBox(num);
        numBox.setAlignment(Pos.TOP_RIGHT);
        cell.getChildren().add(numBox);

        // Events
        String searchText = (searchField.getText() == null) ? "" : searchField.getText().toLowerCase();

        CalendarEvent firstEvent = null; // Store first event to color the box
        int visibleEventCount = 0;

        for (CalendarEvent e : events) {
            LocalDate eStart = e.getStartDateTime().toLocalDate();
            if (eStart.equals(date)) {
                if (!activeFilters.contains(e.getCategory()))
                    continue;
                if (!searchText.isEmpty() && !e.getTitle().toLowerCase().contains(searchText))
                    continue;

                visibleEventCount++;
                if (firstEvent == null)
                    firstEvent = e;

                Label eventLbl = new Label(e.getTitle());
                eventLbl.getStyleClass().add("event-label");
                // Reset internal label style if we are coloring the whole box, or keep it for
                // contrast
                // For now, keep it white text on category color for readability on top of
                // colored box
                String color = e.getColor();
                eventLbl.setStyle("-fx-background-color: " + color
                        + "; -fx-text-fill: white; -fx-background-radius: 3px; -fx-padding: 2px; -fx-cursor: hand;");

                // Make event clickable to show options
                eventLbl.setOnMouseClicked(evt -> {
                    evt.consume(); // Prevent cell click from firing
                    showEventOptionsDialog(e);
                });

                cell.getChildren().add(eventLbl);
            }
        }

        // If there are events, paint the whole box with the first event's category
        // color (faded)
        if (visibleEventCount > 0 && firstEvent != null && !date.equals(selectedDate)) {
            String color = firstEvent.getColor();
            // Apply background with lighter opacity for the whole cell
            cell.setStyle("-fx-background-color: " + color + "33; -fx-border-color: #E0E0E0;");
        } else if (!date.equals(selectedDate)) {
            // Default background
            cell.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0;");
        }

        return cell;
    }

    private void setupSearchField() {
        ContextMenu searchSuggestions = new ContextMenu();
        searchSuggestions.setMinWidth(200); // Set a minimum width for better visibility

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            // 1. Always update the calendar view for live filtering
            updateCalendar();

            // 2. Autocomplete Suggestions Logic
            if (newVal == null || newVal.trim().isEmpty()) {
                searchSuggestions.hide();
            } else {
                List<MenuItem> items = new ArrayList<>();
                String lowerVal = newVal.toLowerCase();
                Set<String> addedTitles = new HashSet<>();

                for (CalendarEvent e : events) {
                    // Check if event title contains search text
                    if (e.getTitle().toLowerCase().contains(lowerVal)) {
                        if (addedTitles.contains(e.getTitle()))
                            continue;

                        addedTitles.add(e.getTitle());
                        MenuItem item = new MenuItem(e.getTitle());
                        item.setOnAction(action -> {
                            // On Selection:
                            // A. Navigate to the event's month
                            currentYearMonth = YearMonth.from(e.getStartDateTime());

                            // B. Set the full title in search box (triggers filter to isolate this event)
                            searchField.setText(e.getTitle());

                            // C. Hide suggestions
                            searchSuggestions.hide();

                            // D. Force update to render the target month with filtered event
                            updateCalendar();
                        });
                        items.add(item);
                    }
                }

                if (!items.isEmpty()) {
                    searchSuggestions.getItems().setAll(items);
                    if (!searchSuggestions.isShowing()) {
                        searchSuggestions.show(searchField, javafx.geometry.Side.BOTTOM, 0, 0);
                    }
                } else {
                    searchSuggestions.hide();
                }
            }
        });
    }

    private void setupCategoryButton(Button btn) {
        ContextMenu contextMenu = new ContextMenu();
        // Add CheckMenuItems for each category
        for (Category c : Category.values()) {
            CheckMenuItem item = new CheckMenuItem(c.getName());
            item.setSelected(true); // Initially all selected
            item.setOnAction(e -> {
                if (item.isSelected()) {
                    activeFilters.add(c.getId());
                } else {
                    activeFilters.remove(c.getId());
                }
                updateCalendar(); // Refresh view
            });
            contextMenu.getItems().add(item);
        }

        btn.setOnAction(e -> {
            contextMenu.show(btn, javafx.geometry.Side.BOTTOM, 0, 0);
        });
    }

    private void showEventOptionsDialog(CalendarEvent event) {
        Alert dialog = new Alert(Alert.AlertType.NONE);
        dialog.setTitle("Event Options");
        dialog.setHeaderText(event.getTitle());

        // Content with event details
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label detailsLabel = new Label("Start: " + event.getStartDateTime().toString().replace('T', ' ') + "\n" +
                "End: " + event.getEndDateTime().toString().replace('T', ' ') + "\n" +
                "Category: " + event.getCategory());
        detailsLabel.setWrapText(true);
        content.getChildren().add(detailsLabel);

        dialog.getDialogPane().setContent(content);

        // Custom buttons
        ButtonType editBtn = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteBtn = new ButtonType("Delete", ButtonBar.ButtonData.OTHER);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getButtonTypes().setAll(editBtn, deleteBtn, cancelBtn);

        // Style the delete button to be red
        Button deleteBtnNode = (Button) dialog.getDialogPane().lookupButton(deleteBtn);
        if (deleteBtnNode != null) {
            deleteBtnNode.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        }

        dialog.showAndWait().ifPresent(response -> {
            if (response == editBtn) {
                editEvent(event);
            } else if (response == deleteBtn) {
                deleteEvent(event);
            }
        });
    }

    private void editEvent(CalendarEvent event) {
        // Find the actual event from the service
        List<Event> dbEvents = eventService.loadEvents();
        Event dbEvent = dbEvents.stream()
                .filter(e -> String.valueOf(e.getId()).equals(event.getId()))
                .findFirst()
                .orElse(null);

        if (dbEvent == null) {
            showAlert("Error", "Event not found");
            return;
        }

        // Create edit dialog
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Edit Event");
        dialog.setHeaderText("Edit " + event.getTitle());

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField(dbEvent.getTitle());
        TextArea descArea = new TextArea(dbEvent.getDescription());
        descArea.setPrefRowCount(3);

        DatePicker startDatePicker = new DatePicker(dbEvent.getStartDateTime().toLocalDate());
        ComboBox<String> startTimeBox = new ComboBox<>();
        startTimeBox.getItems().addAll(generateTimeSlots());
        startTimeBox.setValue(dbEvent.getStartDateTime().toLocalTime().toString());

        DatePicker endDatePicker = new DatePicker(dbEvent.getEndDateTime().toLocalDate());
        ComboBox<String> endTimeBox = new ComboBox<>();
        endTimeBox.getItems().addAll(generateTimeSlots());
        endTimeBox.setValue(dbEvent.getEndDateTime().toLocalTime().toString());

        ComboBox<Category> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(Category.values());
        for (Category c : Category.values()) {
            if (c.getId().equals(dbEvent.getCategory())) {
                categoryBox.setValue(c);
                break;
            }
        }

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Start Date:"), 0, 2);
        grid.add(startDatePicker, 1, 2);
        grid.add(new Label("Start Time:"), 0, 3);
        grid.add(startTimeBox, 1, 3);
        grid.add(new Label("End Date:"), 0, 4);
        grid.add(endDatePicker, 1, 4);
        grid.add(new Label("End Time:"), 0, 5);
        grid.add(endTimeBox, 1, 5);
        grid.add(new Label("Category:"), 0, 6);
        grid.add(categoryBox, 1, 6);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(saveBtn, cancelBtn);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtn) {
                try {
                    dbEvent.setTitle(titleField.getText());
                    dbEvent.setDescription(descArea.getText());

                    LocalTime startTime = LocalTime.parse(startTimeBox.getValue());
                    LocalTime endTime = LocalTime.parse(endTimeBox.getValue());

                    dbEvent.setStartDateTime(LocalDateTime.of(startDatePicker.getValue(), startTime));
                    dbEvent.setEndDateTime(LocalDateTime.of(endDatePicker.getValue(), endTime));

                    if (categoryBox.getValue() != null) {
                        dbEvent.setCategory(categoryBox.getValue().getId());
                    }

                    return dbEvent;
                } catch (Exception e) {
                    showAlert("Error", "Invalid input: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedEvent -> {
            eventService.updateEvent(updatedEvent.getId(), updatedEvent);
            showAlert("Success", "Event updated successfully!");
            loadEventsFromBackend();
            updateCalendar();
        });
    }

    private void deleteEvent(CalendarEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Event");
        confirmation.setContentText(
                "Are you sure you want to delete \"" + event.getTitle() + "\"?\n\nThis action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    int eventId = Integer.parseInt(event.getId());
                    eventService.deleteEvent(eventId);
                    showAlert("Success", "Event deleted successfully!");
                    loadEventsFromBackend();
                    updateCalendar();
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete event: " + e.getMessage());
                }
            }
        });
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
