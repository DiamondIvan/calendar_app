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
import com.example.frontend.context.ThemeManager;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.function.Consumer;

/**
 * CalendarPage displays an interactive monthly calendar view with event
 * management.
 * 
 * This is the main calendar UI component providing:
 * 
 * **Core Features:**
 * - Monthly calendar grid with week-based layout (Sunday-Saturday)
 * - Event display with category-based color coding
 * - Selected date tracking with animated visual feedback
 * - Public holidays integration (always visible)
 * - User-specific event filtering
 * 
 * **Interactive Features:**
 * - Click dates to select/deselect with fade animations
 * - Search events with live filtering and autocomplete suggestions
 * - Category filtering with checkboxes
 * - Date range search dialog
 * - Event creation, editing, and deletion
 * - Month navigation (prev/next)
 * 
 * **Visual Customization:**
 * - 7 predefined color themes with gradients
 * - Theme affects: background, sidebar, buttons, hover states
 * - Theme persistence via ThemeManager
 * 
 * **Data Management:**
 * - Loads events from EventCsvService
 * - Filters events by user ID
 * - Includes Malaysian public holidays for 2026
 * - Real-time calendar updates on data changes
 * 
 * Layout: NavBar + Header Bar + (Left Sidebar | Calendar Grid)
 */
public class CalendarPage {

    /** Navigation callback for routing to other pages */
    private final Consumer<String> navigate;

    /** Service for loading/saving events from CSV */
    private final EventCsvService eventService;

    /** Currently logged-in user (null for anonymous) */
    private final AppUser currentUser;

    /** Current month/year being displayed */
    private YearMonth currentYearMonth;

    /** Currently selected date (null when deselected) */
    private LocalDate selectedDate;

    /** All events (user events + public holidays) */
    private List<CalendarEvent> events;

    /** Set of active category IDs for filtering (all enabled by default) */
    private Set<String> activeFilters = new HashSet<>();

    /** Label showing current month and year in header */
    private Label monthYearLabel;

    /** Grid pane containing the calendar cells */
    private GridPane calendarGrid;

    /** Search text field with autocomplete */
    private TextField searchField;

    /** Map of date to selection overlay for animations */
    private final Map<LocalDate, Region> selectionOverlayByDate = new HashMap<>();

    // Side Panel Components
    /** Sidebar label showing selected day number */
    private Label sideCurrentDateNum;

    /** Sidebar label showing selected month */
    private Label sideCurrentMonth;

    /** Sidebar label showing selected year */
    private Label sideCurrentYear;

    /** Sidebar colored rectangle indicating theme */
    private Rectangle sideColorBox;

    /** Category filter button */
    private Button catBtn;

    /** Create event navigation button */
    private Button createBtn;

    /** Previous month navigation button */
    private Button prevBtn;

    /** Next month navigation button */
    private Button nextBtn;

    /**
     * Converts a JavaFX Color to hex string format.
     * 
     * @param color The JavaFX Color object
     * @return Hex color string (e.g., "#FF5733")
     */
    private static String toHex(Color color) {
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    /**
     * Sets the text fill color of a label using inline CSS.
     * 
     * Safely replaces existing -fx-text-fill style or adds it.
     * Preserves other existing styles.
     * 
     * @param label The label to style (null-safe)
     * @param color The text color
     */
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

    /**
     * Applies a complete theme to the calendar page.
     * 
     * Updates:
     * - Main container background (gradient)
     * - Sidebar color box
     * - Sidebar text colors (date, month, year)
     * - All buttons (colors, borders, hover effects)
     * 
     * Persists theme via ThemeManager for cross-page consistency.
     * 
     * @param mainContainer      The root VBox container
     * @param userSelectedColor  Sidebar/accent color (e.g., "#90caf9")
     * @param buttonColor        Primary button color
     * @param hoverButtonColor   Button hover state color
     * @param backgroundGradient CSS gradient for background
     */
    private void applyTheme(
            VBox mainContainer,
            String userSelectedColor,
            String buttonColor,
            String hoverButtonColor,
            String backgroundGradient) {
        ThemeManager.getInstance().setScheme(userSelectedColor, buttonColor, hoverButtonColor, backgroundGradient);

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

    /**
     * Constructs a CalendarPage with full dependencies.
     * 
     * Initializes:
     * - Current month to today
     * - Selected date to today
     * - All category filters enabled
     * - Loads events from backend (user events + holidays)
     * 
     * @param navigate     Navigation callback for routing
     * @param eventService Service for event data operations
     * @param currentUser  The logged-in user (null for anonymous)
     */
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

    /**
     * Default constructor with no navigation and mock dependencies.
     * Used for testing or standalone instances.
     */
    public CalendarPage() {
        this(null, new EventCsvService(), null);
    }

    /**
     * Loads all events from backend and holiday data.
     * 
     * Process:
     * 1. Clears existing events
     * 2. Loads Malaysian public holidays for 2026
     * 3. Loads user-created events from CSV
     * 4. Filters events by current user ID
     * 5. Converts to CalendarEvent format with category colors
     * 
     * Skips events that fail to parse (defensive).
     */
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
                    Category.HOLIDAY.getColorHex(),
                    Category.HOLIDAY.getId());
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

    /**
     * Resolves a category ID to its corresponding color hex code.
     * 
     * Matches category string (case-insensitive) against Category enum.
     * 
     * @param categoryId The category ID string (can be null)
     * @return Hex color code (defaults to "#4CAF50" green if not found)
     */
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

    /**
     * Builds and returns the complete calendar page UI.
     * 
     * Layout structure:
     * - NavBar (top)
     * - Header bar (month navigation, search, filters, create button)
     * - Content area:
     * - Left sidebar (date display, theme selector)
     * - Right calendar grid (interactive monthly calendar)
     * 
     * Applies persisted theme on load.
     * 
     * @return The complete calendar UI as a Node
     */
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

        // Apply persisted theme on first load
        ThemeManager theme = ThemeManager.getInstance();
        applyTheme(mainContainer, theme.getUserSelectedColor(), theme.getButtonColor(), theme.getHoverButtonColor(),
                theme.getBackgroundGradient());

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

    /**
     * Creates the header bar with navigation and controls.
     * 
     * Contains (left to right):
     * - Prev month button
     * - Month/year label
     * - Next month button
     * - Spacer
     * - Search field with autocomplete
     * - Category filter button
     * - Date search button (📅)
     * - Create event button
     * 
     * @return HBox containing all header elements
     */
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
        setupDateSearchButton(viewBtn);

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

    /**
     * Sets up the date search button action.
     * Opens the date range search dialog when clicked.
     * 
     * @param viewBtn The 📅 button to configure
     */
    private void setupDateSearchButton(Button viewBtn) {
        viewBtn.setOnAction(e -> showDateSearchDialog());
    }

    /**
     * Shows a dialog for searching events by date range.
     * 
     * Features:
     * - Start and end date pickers (default to selected date or today)
     * - Search button to find events in range
     * - Results list showing matching events
     * - Respects current search text and category filters
     * - Displays events sorted by start time
     * 
     * Results format: "YYYY-MM-DD HH:MM - Title (CATEGORY)"
     */
    private void showDateSearchDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Search Events by Date");
        dialog.setHeaderText(null);

        LocalDate defaultDate = (selectedDate != null) ? selectedDate : LocalDate.now();

        DatePicker startPicker = new DatePicker(defaultDate);
        DatePicker endPicker = new DatePicker(defaultDate);

        ListView<String> resultsList = new ListView<>();
        resultsList.setPrefHeight(260);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(new Label("Start date:"), 0, 0);
        grid.add(startPicker, 1, 0);
        grid.add(new Label("End date:"), 0, 1);
        grid.add(endPicker, 1, 1);
        grid.add(new Label("Results:"), 0, 2);
        GridPane.setColumnSpan(resultsList, 2);
        grid.add(resultsList, 0, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType searchBtnType = new ButtonType("Search", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(searchBtnType, ButtonType.CLOSE);

        Button searchBtn = (Button) dialog.getDialogPane().lookupButton(searchBtnType);
        if (searchBtn != null) {
            searchBtn.addEventFilter(ActionEvent.ACTION, evt -> {
                evt.consume();

                LocalDate start = startPicker.getValue();
                LocalDate end = endPicker.getValue();
                if (start == null || end == null) {
                    resultsList.getItems().setAll("Please select both a start and end date.");
                    return;
                }
                if (end.isBefore(start)) {
                    resultsList.getItems().setAll("End date must be on/after start date.");
                    return;
                }

                String searchText = (searchField != null && searchField.getText() != null)
                        ? searchField.getText().trim().toLowerCase()
                        : "";

                List<CalendarEvent> matches = new ArrayList<>();
                for (CalendarEvent ev : events) {
                    LocalDate evDate = ev.getStartDateTime().toLocalDate();
                    if (evDate.isBefore(start) || evDate.isAfter(end)) {
                        continue;
                    }
                    if (!activeFilters.contains(ev.getCategory())) {
                        continue;
                    }
                    if (!searchText.isEmpty() && !ev.getTitle().toLowerCase().contains(searchText)) {
                        continue;
                    }
                    matches.add(ev);
                }

                matches.sort(Comparator.comparing(CalendarEvent::getStartDateTime));

                if (matches.isEmpty()) {
                    resultsList.getItems().setAll("No matching events found for the selected dates.");
                    return;
                }

                List<String> lines = new ArrayList<>();
                for (CalendarEvent ev : matches) {
                    String when = ev.getStartDateTime().toString().replace('T', ' ');
                    lines.add(when + " - " + ev.getTitle() + " (" + ev.getCategory() + ")");
                }
                resultsList.getItems().setAll(lines);
            });
        }

        dialog.showAndWait();
    }

    /**
     * Creates the left sidebar with date display and theme selector.
     * 
     * Components:
     * - Large date number (selected day)
     * - Month name
     * - Year
     * - Colored rectangle (current theme accent)
     * - Theme selector (7 color squares)
     * 
     * Clicking a theme square applies the theme to the entire page.
     * 
     * @param mainContainer The main VBox to apply themes to
     * @return VBox containing the complete sidebar
     */
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

    /**
     * Updates the calendar grid with current month's dates and events.
     * 
     * Process:
     * 1. Clears existing grid and overlay map
     * 2. Updates month/year labels
     * 3. Creates 7-column grid (Sunday-Saturday)
     * 4. Adds header row with day names
     * 5. Adds date cells for:
     * - Previous month filler (hidden)
     * - Current month (visible with events)
     * - Next month filler (hidden)
     * 6. Each cell shows:
     * - Date number
     * - Events matching filters and search
     * - Background color based on first event
     * - Selection overlay if selected
     * 
     * Grid typically spans 6-7 rows to accommodate all dates.
     */
    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        selectionOverlayByDate.clear();

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
            StackPane cell = createDayCell(prevMonth.plusDays(i), true);
            calendarGrid.add(cell, col++, row);
        }

        // Current Month
        int daysInMonth = currentYearMonth.lengthOfMonth();
        for (int i = 0; i < daysInMonth; i++) {
            LocalDate date = firstDay.plusDays(i);
            StackPane cell = createDayCell(date, false);
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
            StackPane cell = createDayCell(nextMonth.plusDays(addedNext++), true);
            calendarGrid.add(cell, col++, row);

            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Animates a selection overlay's opacity with a fade transition.
     * 
     * Uses 160ms fade animation for smooth visual feedback.
     * 
     * @param overlay    The Region to fade (null-safe)
     * @param from       Starting opacity (0.0 to 1.0)
     * @param to         Target opacity (0.0 to 1.0)
     * @param onFinished Callback to run after animation completes (can be null)
     */
    private void fadeSelectionOverlay(Region overlay, double from, double to, Runnable onFinished) {
        if (overlay == null) {
            return;
        }

        FadeTransition ft = new FadeTransition(Duration.millis(160), overlay);
        ft.setFromValue(from);
        ft.setToValue(to);
        ft.setOnFinished(evt -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        ft.play();
    }

    /**
     * Updates the selected date with animated visual feedback.
     * 
     * Behavior:
     * - If clicking already-selected date: Deselects (fade out)
     * - If selecting new date:
     * 1. Fades out old selection
     * 2. Fades in new selection
     * 3. Updates selectedDate field
     * 
     * @param newDate The date to select (ignored if null)
     */
    private void setSelectedDateAnimated(LocalDate newDate) {
        if (newDate == null) {
            return;
        }

        // Toggle off if clicking the already-selected date
        if (this.selectedDate != null && newDate.equals(this.selectedDate)) {
            Region overlay = selectionOverlayByDate.get(this.selectedDate);
            this.selectedDate = null;
            if (overlay != null) {
                overlay.setVisible(true);
                fadeSelectionOverlay(overlay, overlay.getOpacity(), 0.0, () -> overlay.setVisible(false));
            }
            return;
        }

        LocalDate oldDate = this.selectedDate;
        this.selectedDate = newDate;

        Region oldOverlay = (oldDate == null) ? null : selectionOverlayByDate.get(oldDate);
        if (oldOverlay != null) {
            oldOverlay.setVisible(true);
            fadeSelectionOverlay(oldOverlay, oldOverlay.getOpacity(), 0.0, () -> oldOverlay.setVisible(false));
        }

        Region newOverlay = selectionOverlayByDate.get(newDate);
        if (newOverlay != null) {
            newOverlay.setVisible(true);
            newOverlay.setOpacity(0.0);
            fadeSelectionOverlay(newOverlay, 0.0, 1.0, null);
        }
    }

    /**
     * Creates a single calendar day cell.
     * 
     * Cell contents:
     * - Selection overlay (blue border/background when selected)
     * - Date number (top-right)
     * - Event labels (colored badges, clickable)
     * - Background tint (based on first event's category)
     * 
     * Clicking cell:
     * - Updates selection with animation
     * - Updates sidebar date display
     * 
     * Clicking event label:
     * - Opens event options dialog (edit/delete)
     * 
     * Other month cells are invisible.
     * 
     * @param date         The date for this cell
     * @param isOtherMonth True if date is from prev/next month
     * @return StackPane containing the complete day cell
     */
    private StackPane createDayCell(LocalDate date, boolean isOtherMonth) {
        StackPane cellRoot = new StackPane();
        VBox content = new VBox(5);
        cellRoot.getChildren().add(content);

        // Hide other month cells completely
        if (isOtherMonth) {
            cellRoot.setVisible(false);
            return cellRoot;
        }

        cellRoot.getStyleClass().add("day-cell");

        // Selection overlay (so we can fade it in/out without re-rendering)
        Region selectionOverlay = new Region();
        selectionOverlay.setMouseTransparent(true);
        selectionOverlay.setStyle(
                "-fx-background-color: #E3F2FD; -fx-border-color: #2196F3; -fx-border-width: 2px;"
                        + " -fx-effect: dropshadow(gaussian, rgba(33, 150, 243, 0.4), 10, 0, 0, 0);");
        selectionOverlay.prefWidthProperty().bind(cellRoot.widthProperty());
        selectionOverlay.prefHeightProperty().bind(cellRoot.heightProperty());
        boolean isSelected = selectedDate != null && date.equals(selectedDate);
        selectionOverlay.setVisible(isSelected);
        selectionOverlay.setOpacity(isSelected ? 1.0 : 0.0);
        cellRoot.getChildren().add(0, selectionOverlay);
        selectionOverlayByDate.put(date, selectionOverlay);

        // Interaction: Click to update sidebar and visuals
        cellRoot.setOnMouseClicked(e -> {
            setSelectedDateAnimated(date);

            // Update Sidebar
            sideCurrentDateNum.setText(String.valueOf(date.getDayOfMonth()));
            sideCurrentMonth.setText(date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            sideCurrentYear.setText(String.valueOf(date.getYear()));
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
        content.getChildren().add(numBox);

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

                content.getChildren().add(eventLbl);
            }
        }

        // If there are events, paint the whole box with the first event's category
        // color (faded)
        if (visibleEventCount > 0 && firstEvent != null) {
            String color = firstEvent.getColor();
            // Apply background with lighter opacity for the whole cell
            cellRoot.setStyle("-fx-background-color: " + color + "33; -fx-border-color: #E0E0E0;");
        } else {
            // Default background
            cellRoot.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0;");
        }

        return cellRoot;
    }

    /**
     * Sets up search field with live filtering and autocomplete.
     * 
     * Features:
     * 1. **Live filtering**: Updates calendar view as user types
     * 2. **Autocomplete suggestions**:
     * - Shows menu with matching event titles
     * - Click suggestion to:
     * a. Navigate to event's month
     * b. Set search text to exact title
     * c. Update calendar view
     * 3. **Debounced search**: Updates on every keystroke
     * 
     * Suggestions are deduplicated by title.
     */
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

    /**
     * Sets up the category filter button with checkboxes.
     * 
     * Creates a context menu with:
     * - One CheckMenuItem per Category
     * - All checked by default
     * - Checking/unchecking updates activeFilters set
     * - Updates calendar view on change
     * 
     * @param btn The category button to configure
     */
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

    /**
     * Shows a dialog with event options (View/Edit/Delete).
     * 
     * For holidays:
     * - Shows simple info dialog (read-only)
     * 
     * For user events:
     * - Shows dialog with event details
     * - Buttons: Edit, Delete (red), Cancel
     * - Edit: Opens edit form dialog
     * - Delete: Opens confirmation dialog
     * 
     * @param event The CalendarEvent to show options for
     */
    private void showEventOptionsDialog(CalendarEvent event) {
        if (event != null && event.getCategory() != null
                && event.getCategory().equalsIgnoreCase(Category.HOLIDAY.getId())) {
            Alert dialog = new Alert(Alert.AlertType.INFORMATION);
            dialog.setTitle("Holiday");
            dialog.setHeaderText(event.getTitle());
            dialog.setContentText("Public Holiday");
            dialog.showAndWait();
            return;
        }

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

    /**
     * Opens a dialog to edit an existing event.
     * 
     * Form fields:
     * - Title (TextField)
     * - Description (TextArea, 3 rows)
     * - Start Date (DatePicker)
     * - Start Time (ComboBox with 15-min slots)
     * - End Date (DatePicker)
     * - End Time (ComboBox with 15-min slots)
     * - Category (ComboBox with all categories)
     * 
     * On Save:
     * - Updates event via eventService.updateEvent()
     * - Reloads events and refreshes calendar
     * - Shows success message
     * 
     * @param event The CalendarEvent to edit
     */
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

    /**
     * Deletes an event after confirmation.
     * 
     * Shows confirmation dialog with warning.
     * On confirm:
     * - Deletes via eventService.deleteEvent()
     * - Reloads events and refreshes calendar
     * - Shows success/error message
     * 
     * @param event The CalendarEvent to delete
     */
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

    /**
     * Generates time slot strings for time pickers.
     * 
     * Creates 96 time slots (24 hours × 4 quarters):
     * - 00:00, 00:15, 00:30, 00:45, ..., 23:45
     * 
     * @return List of time strings in HH:MM format
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

    /**
     * Shows a simple information alert.
     * 
     * @param title   Alert dialog title
     * @param content Alert message content
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
