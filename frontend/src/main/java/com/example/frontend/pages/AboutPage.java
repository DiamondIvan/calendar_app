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

public class AboutPage {

        @SuppressWarnings("unused")
        private final Consumer<String> navigate;

        public AboutPage(Consumer<String> navigate) {
                this.navigate = navigate;
        }

        public Node getView() {
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

                // --- 1. INFOGRAPHIC CARDS (Top Row) ---
                FlowPane featuresFeatures = new FlowPane();
                featuresFeatures.setAlignment(Pos.CENTER);
                featuresFeatures.setHgap(25);
                featuresFeatures.setVgap(25);
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
                                createTeamMember("angel.jpg", "Tan Ke Qin", ""),
                                createTeamMember("penghan.jpg", "Ng Peng Han", ""));

                // --- 6. ALL FEATURES (Program List) ---
                VBox featuresSection = new VBox(20);
                featuresSection.getStyleClass().add("features-section");
                featuresSection.setMaxWidth(1000);

                Label featTitle = new Label("All Features");
                featTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
                Label featSub = new Label("A comprehensive suite of tools to manage your schedule efficiently.");

                FlowPane featGrid = new FlowPane();
                featGrid.setHgap(20);
                featGrid.setVgap(20);
                featGrid.setAlignment(Pos.CENTER);

                featGrid.getChildren().addAll(
                                createFeatureCard("1. App Launch",
                                                "When the user runs the program, the system loads data and displays the main menu options."),
                                createFeatureCard("2. Create Event",
                                                "Allows users to create new events by entering title, description, start time, and end time."),
                                createFeatureCard("3. View Calendar",
                                                "Displays events in either a weekly list view or a monthly calendar view."),
                                createFeatureCard("4. Update or Delete Event",
                                                "Users can update event details or delete events using the event ID."),
                                createFeatureCard("5. Search Event",
                                                "Search for events by specific date or date range. Quickly find matches."),
                                createFeatureCard("6. Backup and Restore",
                                                "Back up all event data to a file for safekeeping, or restore data from a backup."),
                                createFeatureCard("7. Exit Application",
                                                "Safely exit the application. All data is automatically saved before closing."),
                                createFeatureCard("8. Optional Features",
                                                "Enhanced functionality including reminders, conflict detection, and statistics."));

                featuresSection.getChildren().addAll(featTitle, featSub, featGrid);

                // Combine All
                contentWrapper.getChildren().addAll(
                                featuresFeatures,
                                dsSection,
                                teamTitle, teamSub, teamBox,
                                featuresSection);

                rootStack.getChildren().add(contentWrapper);
                scrollPane.setContent(rootStack);
                return scrollPane;
        }

        // Helper: Top 4 Cards
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

        // Helper: Custom Box for Code Tags
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

        private Label createCodeTag(String text) {
                Label lbl = new Label(text);
                // Light pink bg, red text, monospace font
                lbl.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-family: 'Consolas', 'Monospace'; -fx-font-size: 11px;");
                return lbl;
        }

        private Label createPlainLabel(String text) {
                Label lbl = new Label(text);
                // Ensure text is black/dark grey and readable
                lbl.setStyle("-fx-text-fill: #333; -fx-font-size: 12px;");
                return lbl;
        }

        // Helper: Data Structure Box inside Blue Section
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

        // Helper: Team Member
        private VBox createTeamMember(String imgName, String name, String role) {
                VBox box = new VBox(10);
                box.setAlignment(Pos.CENTER);
                box.setPadding(new Insets(10));

                // Container for uniform circular image
                StackPane imgContainer = new StackPane();
                imgContainer.setPrefSize(100, 100);
                imgContainer.setMaxSize(100, 100);
                imgContainer.setMinSize(100, 100);
                // Circular Clip - Correctly centered at (50,50)
                Circle clip = new Circle(50, 50, 50);
                imgContainer.setClip(clip);

                ImageView iv = new ImageView();
                iv.setPreserveRatio(true);

                try {
                        InputStream is = getClass().getResourceAsStream("/frontend/public/" + imgName);
                        if (is != null) {
                                Image img = new Image(is);
                                iv.setImage(img);

                                // "Cover" logic: ensure the image fills the 100x100 circle
                                if (img.getWidth() > img.getHeight()) {
                                        iv.setFitHeight(100);
                                } else {
                                        iv.setFitWidth(100);
                                }
                        }
                } catch (Exception e) {
                        System.out.println("No img: " + imgName);
                }

                // If image failed to load, show a colored circle
                if (iv.getImage() == null) {
                        Circle placeholder = new Circle(50, Color.LIGHTGRAY);
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

        // Helper: Feature List Card
        private VBox createFeatureCard(String title, String desc) {
                VBox card = new VBox(8);
                card.getStyleClass().add("feature-list-card");
                Label t = new Label(title);
                t.getStyleClass().add("feature-list-title");
                Label d = new Label(desc);
                d.getStyleClass().add("feature-list-desc");
                card.getChildren().addAll(t, d);
                return card;
        }

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