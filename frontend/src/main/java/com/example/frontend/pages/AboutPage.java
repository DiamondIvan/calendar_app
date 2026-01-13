package com.example.frontend.pages;

import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import java.io.InputStream;
import java.util.function.Consumer;
import com.example.frontend.components.NavBar;

/**
 * AboutPage displays comprehensive information about the calendar application.
 * 
 * This page serves as the application's "About" or "Information" page,
 * showcasing:
 * - Project mission and solution overview
 * - Technical architecture and data structures (CSV files)
 * - Team member profiles with photos
 * - Complete feature list (8 core features)
 * - Marks distribution breakdown
 * - Instructor contact information
 * 
 * Design features:
 * - Scrollable content with multiple sections
 * - Rich visual elements (images, icons, cards)
 * - Modal dialog for problem statement
 * - Responsive layout with grid-based feature cards
 * - Color-coded sections for visual hierarchy
 */
public class AboutPage {

        /** Navigation callback for routing to other pages */
        private final Consumer<String> navigate;

        /**
         * Constructs an AboutPage with navigation callback.
         * 
         * @param navigate Navigation callback for routing between pages
         */
        public AboutPage(Consumer<String> navigate) {
                this.navigate = navigate;
        }

        /**
         * Builds and returns the complete About page UI.
         * 
         * The page is organized into the following sections:
         * 
         * 1. **Hero Section**: Title and "View Problem Statement" button
         * 2. **Infographic Cards**: Four mission/solution cards with icons
         * 3. **Data Structure**: Technical details about CSV storage and files
         * 4. **Team Section**: Profile cards for all 5 team members
         * 5. **Features Section**: Grid of 8 feature cards
         * 6. **Marks Distribution**: Three cards showing grade breakdown
         * 7. **Contact Section**: Instructor information
         * 8. **Footer**: Assignment information
         * 
         * All content is wrapped in a scrollable pane for easy navigation.
         * The page uses AboutPage.css for styling.
         * 
         * @return A ScrollPane containing the complete About page layout
         */
        public Node getView() {
                // Create Navigation Bar
                NavBar navBar = new NavBar(navigate);

                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setFitToWidth(true);
                scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

                // Root StackPane for Background Gradient and Centering
                StackPane rootStack = new StackPane();
                rootStack.setAlignment(Pos.TOP_CENTER);
                rootStack.getStyleClass().add("about-page");
                rootStack.setPadding(new Insets(40, 0, 40, 0));

                // Content Card (White Box)
                VBox contentWrapper = new VBox(30);
                contentWrapper.setAlignment(Pos.TOP_CENTER);
                contentWrapper.getStyleClass().add("about-content-wrapper");
                // Removed ThemeManager.applyBackground(contentWrapper) so it uses CSS
                // background (white)

                try {
                        rootStack.getStylesheets().add(
                                        getClass().getResource("/frontend/CSS_SubPage/AboutPage.css").toExternalForm());
                } catch (Exception e) {
                }

                // Add navbar at the top of content
                contentWrapper.getChildren().add(navBar);

                // --- HERO SECTION ---
                VBox heroSection = new VBox(20);
                heroSection.setAlignment(Pos.CENTER);
                heroSection.setPadding(new Insets(64, 0, 64, 0));

                Label heroTitle = new Label("Calendar & Scheduler App");
                heroTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #333;");

                Button viewObjectiveBtn = new Button("VIEW PROBLEM STATEMENT");
                viewObjectiveBtn.setStyle("-fx-background-color: #4e73df; -fx-text-fill: white; -fx-font-size: 16px; " +
                                "-fx-padding: 12 24; -fx-background-radius: 5; -fx-cursor: hand;");
                viewObjectiveBtn.setOnMouseEntered(e -> viewObjectiveBtn.setStyle(
                                "-fx-background-color: #3f5fb6; -fx-text-fill: white; -fx-font-size: 16px; " +
                                                "-fx-padding: 12 24; -fx-background-radius: 5; -fx-cursor: hand;"));
                viewObjectiveBtn.setOnMouseExited(e -> viewObjectiveBtn.setStyle(
                                "-fx-background-color: #4e73df; -fx-text-fill: white; -fx-font-size: 16px; " +
                                                "-fx-padding: 12 24; -fx-background-radius: 5; -fx-cursor: hand;"));
                viewObjectiveBtn.setOnAction(e -> showProblemStatementModal());

                heroSection.getChildren().addAll(heroTitle, viewObjectiveBtn);

                // --- 1. INFOGRAPHIC CARDS (Top Row) ---
                HBox featuresFeatures = new HBox(25);
                featuresFeatures.setAlignment(Pos.CENTER);
                // Set padding to separate from top
                featuresFeatures.setPadding(new Insets(0, 0, 30, 0));

                featuresFeatures.getChildren().addAll(
                                createCard("trophy.png", "The Mission",
                                                "Time management is an essential skill for students and professionals alike. Our goal is to build a barebones calendar and schedular app intended for personal use."),
                                createCard("lightbulb.png", "The Solution",
                                                "We provide a simple interface to organize time and commitments legibly. The app creates and manages events, produces views, and performs backups."),
                                createCard("calendar.png", "No Database",
                                                "We are not allowed to use a DBMS. Instead, we master File I/O by keeping data in local .csv files, such as event.csv and recurrent.csv."),
                                createCard("bar-chart.png", "Core Skills",
                                                "This project applies programming fundamentals in Java, focusing specifically on logic flow, Object-Oriented Programming (OOP), and file I/O handling."));

                // --- 2. DATA STRUCTURE SECTION (Blue Box) ---
                VBox dsSection = new VBox(20);
                dsSection.getStyleClass().add("data-structure-box");
                dsSection.setMaxWidth(1000);

                Label dsTitle = new Label("Data Structure");
                dsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
                Label dsDesc = new Label(
                                "We prioritize local storage for portability. The system generates IDs automatically by increment. We handle recurring events (daily, weekly) ensuring they do not repeat indefinitely.");
                dsDesc.setWrapText(true);
                dsDesc.setMaxWidth(800);

                HBox dsContent = new HBox(40);
                dsContent.setAlignment(Pos.CENTER);

                // Left: CSV Files & Technical Goals
                VBox filesBox = new VBox(20);
                filesBox.setMinWidth(400);
                filesBox.setMaxWidth(500);

                // --- Box 1: event.csv ---
                VBox box1Content = new VBox(5);
                box1Content.getChildren().add(createPlainLabel("Stores standard event data:"));

                // Line: eventId (Auto-generated)
                HBox b1l1 = new HBox(5, createCodeTag("eventId"), createPlainLabel("(Auto-generated)"));
                b1l1.setAlignment(Pos.CENTER_LEFT);

                // Line: title , description
                HBox b1l2 = new HBox(5, createCodeTag("title"), createPlainLabel(","), createCodeTag("description"));
                b1l2.setAlignment(Pos.CENTER_LEFT);

                // Line: startDateTime , endDateTime
                HBox b1l3 = new HBox(5, createCodeTag("startDateTime"), createPlainLabel(","),
                                createCodeTag("endDateTime"));
                b1l3.setAlignment(Pos.CENTER_LEFT);

                box1Content.getChildren().addAll(b1l1, b1l2, b1l3);

                // --- Box 2: recurrent.csv ---
                VBox box2Content = new VBox(5);
                HBox b2Header = new HBox(5, createPlainLabel("Linked by"), createCodeTag("eventId"));
                b2Header.setAlignment(Pos.CENTER_LEFT);
                box2Content.getChildren().add(b2Header);

                HBox b2l1 = new HBox(5, createCodeTag("recurrentInterval"), createPlainLabel("(e.g., 1d, 1w)"));
                b2l1.setAlignment(Pos.CENTER_LEFT);

                HBox b2l2 = new HBox(5, createCodeTag("recurrentTimes"), createPlainLabel("(Frequency)"));
                b2l2.setAlignment(Pos.CENTER_LEFT);

                HBox b2l3 = new HBox(5, createCodeTag("recurrentEndDate"), createPlainLabel("(Limit)"));
                b2l3.setAlignment(Pos.CENTER_LEFT);

                box2Content.getChildren().addAll(b2l1, b2l2, b2l3);

                // --- Box 3: Technical Goals (Created as CustomBox for consistency) ---
                VBox box3Content = new VBox(2);
                box3Content.getChildren().addAll(
                                createPlainLabel("Mastering Git version control"),
                                createPlainLabel("Avoiding messy code (Modularity)"),
                                createPlainLabel("Clean, efficient algorithms"));

                filesBox.getChildren().addAll(
                                createCustomBox("event.csv", "Primary Storage", box1Content),
                                createCustomBox("recurrent.csv", "Complex Logic", box2Content),
                                createCustomBox("Technical Goals", "Programming Mastery", box3Content));

                // Right: Screenshot
                ImageView calendarScreen = new ImageView();
                calendarScreen.setFitWidth(320); // Slightly smaller to fit better
                calendarScreen.setPreserveRatio(true);
                try {
                        // Try loading screenshot
                        InputStream is = getClass().getResourceAsStream("/frontend/public/calendar-screenshot.png");
                        if (is != null)
                                calendarScreen.setImage(new Image(is));
                } catch (Exception e) {
                }

                Label screenTitle = new Label("Calendar Interface");
                screenTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");
                Label screenDesc = new Label(
                                "The application provides an intuitive calendar view showing all events at a glance. Users can easily navigate between months, view event details, and manage their schedule efficiently.");
                screenDesc.setWrapText(true);
                screenDesc.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

                VBox rightDS = new VBox(15, calendarScreen, screenTitle, screenDesc);
                rightDS.getStyleClass().add("feature-box"); // Re-use white card style
                rightDS.setAlignment(Pos.TOP_LEFT);
                rightDS.setPadding(new Insets(20));
                rightDS.setMaxWidth(360); // Constrain width so it looks like a card

                dsContent.getChildren().addAll(filesBox, rightDS);
                dsContent.setAlignment(Pos.CENTER); // Center the two columns
                dsSection.getChildren().addAll(dsTitle, dsDesc, dsContent);

                // --- 3. TEAM SECTION ---
                Label teamTitle = new Label("PROJECT TEAM");
                teamTitle.getStyleClass().add("section-label");
                Label teamSub = new Label("Developing the Calendar App using Java and CSV storage.");
                teamSub.setStyle("-fx-text-fill: #777;");

                HBox teamBox = new HBox(25);
                teamBox.setAlignment(Pos.CENTER);

                // Add Team Members
                // Note: Images expected in /frontend/public/
                teamBox.getChildren().addAll(
                                createTeamMember("mak-jia-hng.jpg", "Mak Jia Hng", ""),
                                createTeamMember("tiew.jpg", "Tiew Yi Nuo", ""),
                                createTeamMember("fong.jpg", "Fong Jun Toh", "(Group Leader)"),
                                createTeamMember("angel.jpg", "Angel Tan Ke Qin", ""),
                                createTeamMember("penghan.jpg", "Ng Peng Han", ""));

                // --- 6. ALL FEATURES (Program List) ---
                VBox featuresSection = new VBox(20);
                featuresSection.getStyleClass().add("features-section");
                featuresSection.setMaxWidth(1000);
                featuresSection.setAlignment(Pos.CENTER);

                Label featTitle = new Label("All Features");
                featTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
                Label featSub = new Label("A comprehensive suite of tools to manage your schedule efficiently.");
                featSub.setStyle("-fx-text-fill: #666;");

                // Container for the grid with padding
                VBox gridContainer = new VBox(20);
                gridContainer.setPadding(new Insets(20));
                gridContainer.setAlignment(Pos.CENTER);

                GridPane featGrid = new GridPane();
                featGrid.setHgap(20);
                featGrid.setVgap(20);
                featGrid.setAlignment(Pos.CENTER);

                // Add feature cards in a 2-column grid
                featGrid.add(createFeatureCard("1. App Launch",
                                "When the user runs the program, the system loads data and displays the main menu options."),
                                0, 0);
                featGrid.add(createFeatureCard("2. Create Event",
                                "Allows users to create new events by entering title, description, start time, and end time."),
                                1, 0);
                featGrid.add(createFeatureCard("3. View Calendar",
                                "Displays events in either a weekly list view or a monthly calendar view."), 0, 1);
                featGrid.add(createFeatureCard("4. Update or Delete Event",
                                "Users can update event details or delete events using the event ID."), 1, 1);
                featGrid.add(createFeatureCard("5. Search Event",
                                "Search for events by specific date or date range. Quickly find matches."), 0, 2);
                featGrid.add(createFeatureCard("6. Backup and Restore",
                                "Back up all event data to a file for safekeeping, or restore data from a backup."), 1,
                                2);
                featGrid.add(createFeatureCard("7. Exit Application",
                                "Safely exit the application. All data is automatically saved before closing."), 0, 3);
                featGrid.add(createFeatureCard("8. Optional Features",
                                "Enhanced functionality including reminders, conflict detection, and statistics."), 1,
                                3);

                gridContainer.getChildren().add(featGrid);
                featuresSection.getChildren().addAll(featTitle, featSub, gridContainer);

                // --- 7. MARKS DISTRIBUTION SECTION ---
                VBox marksSection = new VBox(20);
                marksSection.setAlignment(Pos.CENTER);
                marksSection.setPadding(new Insets(40, 20, 40, 20));
                marksSection.setMaxWidth(1200);

                Label marksTitle = new Label("MARKS DISTRIBUTION");
                marksTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
                Label marksSub = new Label("Breakdown of the 12 Marks Available");
                marksSub.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");

                HBox marksCards = new HBox(25);
                marksCards.setAlignment(Pos.CENTER);

                // Card 1: Basic Requirements
                VBox basicCard = createMarksCard(
                                "Basic Requirements",
                                null,
                                new String[] { "Event Creation", "Update & Delete", "Recurring Events",
                                                "Backup & Restore", "" },
                                "Complete Command",
                                "Total Basic",
                                "#4e73df");

                // Card 2: Tech Stack
                VBox techCard = createMarksCard(
                                "Tech Stack",
                                null,
                                new String[] { "Language: Java", "Concept: OOP Principles", "Storage: File I/O (CSV)",
                                                "Collaboration: Git/GitHub", "" },
                                "Structured Architecture",
                                "Programming Skills",
                                "#36b9cc");

                // Card 3: Extra Features
                VBox extraCard = createMarksCard(
                                "Extra Features",
                                null,
                                new String[] { "Log in / Sign Up ", "Interactive GUI", "Statistics",
                                                "Settings for Calendar", "Advanced Search" },
                                "Dynamic Control",
                                "Maximum Extra",
                                "#1cc88a");

                marksCards.getChildren().addAll(basicCard, techCard, extraCard);
                marksSection.getChildren().addAll(marksTitle, marksSub, marksCards);

                // --- 8. CONTACT SECTION ---
                VBox contactSection = new VBox(15);
                contactSection.setAlignment(Pos.CENTER);
                contactSection.setPadding(new Insets(40, 20, 40, 20));
                contactSection.setStyle("-fx-background-color: #f8f9fc;");

                Label contactHeader = new Label("Contact Instructor");
                contactHeader.setStyle(
                                "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-border-color: #1cc88a; -fx-border-width: 0 0 2 0; -fx-padding: 0 0 5 0;");

                Label contactName = new Label("Jonas Chuan");
                contactName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

                Label contactDesc = new Label("For any questions or clarifications.");
                contactDesc.setStyle("-fx-text-fill: #666;");

                Label contactLocation = new Label("📍 Faculty of Computer Science");
                contactLocation.setStyle("-fx-text-fill: #1cc88a; -fx-font-size: 14px;");

                Label contactPhone = new Label("📞 019-5187978");
                contactPhone.setStyle("-fx-text-fill: #1cc88a; -fx-font-size: 14px;");

                Label contactEmail = new Label("✉ jc.chuan.0303@gmail.com");
                contactEmail.setStyle("-fx-text-fill: #1cc88a; -fx-font-size: 14px;");

                contactSection.getChildren().addAll(contactHeader, contactName, contactDesc, contactLocation,
                                contactPhone, contactEmail);

                // --- 9. FOOTER ---
                VBox footer = new VBox(10);
                footer.setAlignment(Pos.CENTER);
                footer.setPadding(new Insets(30, 20, 30, 20));
                footer.setStyle("-fx-background-color: #2c3e50;");

                Label footerText = new Label("FOP Group Assignment 2025/26");
                footerText.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

                footer.getChildren().add(footerText);

                // Add remaining sections (navbar already added at top)
                contentWrapper.getChildren().addAll(
                                heroSection,
                                featuresFeatures,
                                dsSection,
                                teamTitle, teamSub, teamBox,
                                featuresSection,
                                marksSection,
                                contactSection,
                                footer);

                rootStack.getChildren().add(contentWrapper);
                scrollPane.setContent(rootStack);
                return scrollPane;
        }

        /**
         * Creates an informational card with icon, title, and description.
         * 
         * Used for the top row of mission/solution cards.
         * Each card displays an icon image, bold title, and descriptive text.
         * 
         * @param iconName Filename of the icon image (in /frontend/public/)
         * @param title    Bold title text for the card
         * @param desc     Description text (supports wrapping)
         * @return A styled VBox card component
         */
        private VBox createCard(String iconName, String title, String desc) {
                VBox card = new VBox(15);
                card.getStyleClass().add("about-card");

                ImageView iv = new ImageView();
                iv.setFitWidth(50);
                iv.setFitHeight(50);
                try {
                        InputStream is = getClass().getResourceAsStream("/frontend/public/" + iconName);
                        if (is != null)
                                iv.setImage(new Image(is));
                } catch (Exception e) {
                }

                Label t = new Label(title);
                t.getStyleClass().add("card-title");
                Label d = new Label(desc);
                d.getStyleClass().add("card-desc");
                card.getChildren().addAll(iv, t, d);
                return card;
        }

        /**
         * Creates a custom box for displaying code/technical information.
         * 
         * Used in the data structure section to display CSV file details.
         * Combines a title, subtitle, and custom content node.
         * 
         * @param title   Main title (e.g., "event.csv")
         * @param sub     Subtitle text (e.g., "Primary Storage")
         * @param content Custom content node (usually VBox with labels)
         * @return A styled VBox with white background
         */
        private VBox createCustomBox(String title, String sub, Node content) {
                VBox box = new VBox(8);
                box.getStyleClass().add("feature-box");

                Label t = new Label(title);
                t.setStyle("-fx-font-weight:bold; -fx-font-size:16px; -fx-text-fill: #333;");
                Label s = new Label(sub);
                s.setStyle("-fx-font-weight:bold; -fx-text-fill:#333; -fx-font-size: 13px;");

                // Content container
                box.getChildren().addAll(t, s, content);
                return box;
        }

        /**
         * Creates a code-styled label with monospace font.
         * 
         * Used for displaying technical terms like field names (e.g., "eventId").
         * Styled with pink background and red text for code emphasis.
         * 
         * @param text The code text to display
         * @return A styled Label with code appearance
         */
        private Label createCodeTag(String text) {
                Label lbl = new Label(text);
                // Light pink bg, red text, monospace font
                lbl.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-family: 'Consolas', 'Monospace'; -fx-font-size: 11px;");
                return lbl;
        }

        /**
         * Creates a plain text label with standard styling.
         * 
         * Used for regular descriptive text in data structure boxes.
         * 
         * @param text The text to display
         * @return A styled Label with dark text
         */
        private Label createPlainLabel(String text) {
                Label lbl = new Label(text);
                // Ensure text is black/dark grey and readable
                lbl.setStyle("-fx-text-fill: #333; -fx-font-size: 12px;");
                return lbl;
        }

        /**
         * Creates a feature information box (currently unused).
         * 
         * Originally designed for displaying feature details with title,
         * subtitle, and description.
         * 
         * @param title Feature title
         * @param sub   Feature subtitle
         * @param desc  Feature description
         * @return A styled VBox component
         */
        @SuppressWarnings("unused")
        private VBox createFeatureBox(String title, String sub, String desc) {
                VBox box = new VBox(5);
                box.getStyleClass().add("feature-box");
                Label t = new Label(title);
                t.setStyle("-fx-font-weight:bold; -fx-font-size:16px; -fx-text-fill: #333;");
                Label s = new Label(sub);
                s.setStyle("-fx-font-weight:bold; -fx-text-fill:#666; -fx-font-size: 12px;");
                Label d = new Label(desc);
                d.setWrapText(true);
                d.setStyle("-fx-font-size: 12px;");
                box.getChildren().addAll(t, s, d);
                return box;
        }

        /**
         * Creates a team member profile card with photo and name.
         * 
         * Displays a circular profile photo (150x150px) with the member's name
         * and optional role below. If the image fails to load, shows a gray
         * circle placeholder.
         * 
         * The image is cropped to fit the circular frame using "cover" logic
         * (fills the circle while maintaining aspect ratio).
         * 
         * @param imgName Filename of the member's photo (in /frontend/public/)
         * @param name    Member's full name
         * @param role    Optional role text (e.g., "(Group Leader)"), can be empty
         * @return A VBox containing the profile card
         */
        private VBox createTeamMember(String imgName, String name, String role) {
                VBox box = new VBox(10);
                box.setAlignment(Pos.CENTER);
                box.setPadding(new Insets(10));

                // Container for uniform circular image
                StackPane imgContainer = new StackPane();
                imgContainer.setPrefSize(150, 150);
                imgContainer.setMaxSize(150, 150);
                imgContainer.setMinSize(150, 150);
                // Circular Clip - Correctly centered at (75,75)
                Circle clip = new Circle(75, 75, 75);
                imgContainer.setClip(clip);

                ImageView iv = new ImageView();
                iv.setPreserveRatio(true);

                try {
                        InputStream is = getClass().getResourceAsStream("/frontend/public/" + imgName);
                        if (is != null) {
                                Image img = new Image(is);
                                iv.setImage(img);

                                // "Cover" logic: ensure the image fills the 150x150 circle
                                if (img.getWidth() > img.getHeight()) {
                                        iv.setFitHeight(150);
                                } else {
                                        iv.setFitWidth(150);
                                }
                        }
                } catch (Exception e) {
                        System.out.println("No img: " + imgName);
                }

                // If image failed to load, show a colored circle
                if (iv.getImage() == null) {
                        Circle placeholder = new Circle(75, Color.LIGHTGRAY);
                        box.getChildren().add(placeholder);
                } else {
                        imgContainer.getChildren().add(iv);
                        box.getChildren().add(imgContainer);
                }

                Label n = new Label(name);
                n.getStyleClass().add("team-name");

                box.getChildren().add(n);

                if (!role.isEmpty()) {
                        Label r = new Label(role);
                        r.getStyleClass().add("team-role");
                        box.getChildren().add(r);
                }

                return box;
        }

        /**
         * Creates a marks distribution card showing grade breakdown.
         * 
         * Displays a colored header with title, list of items, and total score.
         * Used in the marks distribution section to show:
         * - Basic Requirements (blue)
         * - Tech Stack (cyan)
         * - Extra Features (green)
         * 
         * @param title       Card title (e.g., "Basic Requirements")
         * @param buttonText  Optional button text (null if no button needed)
         * @param items       Array of item descriptions to list
         * @param totalLabel  Large text for total (e.g., "Complete Command")
         * @param totalDesc   Small text below total (e.g., "Total Basic")
         * @param accentColor Hex color for header background (e.g., "#4e73df")
         * @return A styled VBox card with colored header
         */
        private VBox createMarksCard(String title, String buttonText, String[] items, String totalLabel,
                        String totalDesc, String accentColor) {
                VBox card = new VBox();
                card.setAlignment(Pos.TOP_CENTER);
                card.setStyle(
                                "-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
                card.setPrefWidth(320);

                // Header
                VBox header = new VBox(10);
                header.setAlignment(Pos.CENTER);
                header.setPadding(new Insets(20));
                header.setStyle("-fx-background-color: " + accentColor
                                + "; -fx-background-radius: 5 5 0 0; -fx-border-radius: 5 5 0 0;");

                Label headerTitle = new Label(title);
                headerTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

                header.getChildren().add(headerTitle);

                // Only add button if buttonText is provided
                if (buttonText != null && !buttonText.isEmpty()) {
                        Button headerBtn = new Button(buttonText);
                        headerBtn.setStyle(
                                        "-fx-background-color: #1cc88a; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-cursor: hand;");
                        headerBtn.setOnMouseEntered(e -> headerBtn.setStyle(
                                        "-fx-background-color: #17a673; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-cursor: hand;"));
                        headerBtn.setOnMouseExited(e -> headerBtn.setStyle(
                                        "-fx-background-color: #1cc88a; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-cursor: hand;"));
                        header.getChildren().add(headerBtn);
                }

                // Items
                VBox itemsBox = new VBox();
                for (String item : items) {
                        Label itemLabel = new Label(item);
                        itemLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 13px;");
                        itemLabel.setPadding(new Insets(12, 16, 12, 16));
                        itemLabel.setMaxWidth(Double.MAX_VALUE);
                        itemLabel.setAlignment(Pos.CENTER);
                        itemsBox.getChildren().add(itemLabel);
                }

                // Total
                VBox totalBox = new VBox(5);
                totalBox.setAlignment(Pos.CENTER);
                totalBox.setPadding(new Insets(20));

                Label totalLbl = new Label(totalLabel);
                totalLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #333;");

                Label totalDescLbl = new Label(totalDesc);
                totalDescLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

                totalBox.getChildren().addAll(totalLbl, totalDescLbl);

                card.getChildren().addAll(header, itemsBox, totalBox);
                return card;
        }

        /**
         * Displays a modal dialog with the project's problem statement.
         * 
         * Shows a popup window containing:
         * - Project objective title
         * - "Optimize Limited Daytime" subheading
         * - Description of time management importance
         * - Core task details (build calendar app with Java/OOP)
         * - Constraint (no databases, must use CSV files)
         * - Assignment topic reference
         * 
         * Triggered when user clicks "VIEW PROBLEM STATEMENT" button.
         */
        private void showProblemStatementModal() {
                Alert modal = new Alert(Alert.AlertType.NONE);
                modal.setTitle("Project Objective");

                VBox content = new VBox(15);
                content.setPadding(new Insets(20));
                content.setStyle("-fx-background-color: white;");

                Label header = new Label("Project Objective");
                header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");

                Label subheader = new Label("Optimize Limited Daytime ⏰");
                subheader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4e73df;");

                Label desc1 = new Label(
                                "Time management is essential. This app organizes time and commitments in a legible way.");
                desc1.setWrapText(true);
                desc1.setStyle("-fx-text-fill: #555;");

                Label coreTask = new Label(
                                "Core Task: Build a Calendar and Scheduler App for personal use using Java (OOP, File I/O).");
                coreTask.setWrapText(true);
                coreTask.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

                Label constraint = new Label(
                                "Constraint: No databases allowed. Must use local .csv files (event.csv, recurrent.csv).");
                constraint.setWrapText(true);
                constraint.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

                Label footer = new Label("Assignment Topic 2");
                footer.setStyle("-fx-text-fill: #777; -fx-font-style: italic;");

                content.getChildren().addAll(header, subheader, desc1, coreTask, constraint, footer);

                modal.getDialogPane().setContent(content);
                modal.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

                modal.showAndWait();
        }

        /**
         * Creates a feature card for the feature grid.
         * 
         * Each card has a white background with shadow effect,
         * displays a numbered title (e.g., "1. App Launch") and
         * description text.
         * 
         * @param title Feature title with number (e.g., "1. App Launch")
         * @param desc  Feature description text
         * @return A styled VBox card (350px wide)
         */
        private VBox createFeatureCard(String title, String desc) {
                VBox card = new VBox(8);
                card.getStyleClass().add("feature-list-card");
                card.setPrefWidth(350);
                card.setMinHeight(100);
                card.setAlignment(Pos.TOP_LEFT);
                card.setPadding(new Insets(20));
                card.setStyle("-fx-background-color: white; " +
                                "-fx-background-radius: 10; " +
                                "-fx-border-color: #E0E0E0; " +
                                "-fx-border-radius: 10; " +
                                "-fx-border-width: 1; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2);");

                Label t = new Label(title);
                t.getStyleClass().add("feature-list-title");
                t.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");

                Label d = new Label(desc);
                d.getStyleClass().add("feature-list-desc");
                d.setWrapText(true);
                d.setStyle("-fx-text-fill: #555; -fx-font-size: 13px;");

                card.getChildren().addAll(t, d);
                return card;
        }

        /**
         * Shows a simple problem statement alert (currently unused).
         * 
         * Alternative to the modal version, displays project objective
         * in a basic information alert dialog.
         */
        @SuppressWarnings("unused")
        private void showProblemStatement() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Project Objective");
                alert.setHeaderText("The Mission");
                alert.setContentText(
                                "Time management is an essential skill. Our goal is to build a barebones calendar and scheduler app intended for personal use, without using a database.");
                alert.showAndWait();
        }
}