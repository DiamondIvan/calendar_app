package com.example.frontend.pages;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.function.Consumer;

public class AboutPage {

        private Consumer<String> navigate;

        public AboutPage() {
        }

        public AboutPage(Consumer<String> navigate) {
                this.navigate = navigate;
        }

        public Node getView() {
                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color: transparent;");

                // 1. NavBar at the Top (Fixed)
                // 1. NavBar at the Top (Fixed)
                if (navigate != null) {
                        com.example.frontend.components.NavBar navBar = new com.example.frontend.components.NavBar(
                                        navigate);
                        // Ensure visibility with a background and shadow
                        navBar.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
                        root.setTop(navBar);
                }

                // 2. Scrollable Content
                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setFitToWidth(true);
                scrollPane.setStyle("-fx-background-color: transparent;");

                VBox contentWrapper = new VBox(20);
                contentWrapper.setAlignment(Pos.TOP_CENTER);
                contentWrapper.getStyleClass().add("about-content-wrapper");

                try {
                        contentWrapper.getStylesheets()
                                        .add(getClass().getResource("/frontend/CSS_SubPage/AboutPage.css")
                                                        .toExternalForm());
                } catch (Exception e) {
                }

                Label title = new Label("Calendar & Scheduler App");
                title.getStyleClass().add("title-label");

                Button problemStmtBtn = new Button("VIEW PROBLEM STATEMENT");
                problemStmtBtn.getStyleClass().add("button");
                problemStmtBtn.setOnAction(e -> showProblemStatement());

                VBox heroBox = new VBox(15, title, problemStmtBtn);
                heroBox.setAlignment(Pos.CENTER);

                Label aboutTitle = new Label("ABOUT THE PROJECT");
                aboutTitle.getStyleClass().add("section-label");

                Label missionText = new Label("Optimizing the limited daytime every single person gets.");
                missionText.getStyleClass().add("subtitle-label");

                FlowPane featuresFeatures = new FlowPane();
                featuresFeatures.setAlignment(Pos.CENTER);
                featuresFeatures.setHgap(20);
                featuresFeatures.setVgap(20);

                featuresFeatures.getChildren().addAll(
                                createCard("The Mission",
                                                "Time management is an essential skill. Our goal is to build a barebones calendar app."),
                                createCard("The Solution",
                                                "We provide a simple interface to organize time and commitments legibly."),
                                createCard("No Database", "No DBMS allowed. We master File I/O (CSV)."),
                                createCard("Core Skills",
                                                "Project applies programming fundamentals in Java, OOP, and File I/O."));

                Label dsTitle = new Label("Data Structure");
                dsTitle.getStyleClass().add("section-label");

                HBox dsContainer = new HBox(20);
                dsContainer.setAlignment(Pos.TOP_CENTER);

                VBox leftDS = new VBox(15);
                leftDS.getChildren().addAll(
                                createFeatureBox("event.csv", "Primary Storage", "Stores standard event data."),
                                createFeatureBox("recurrent.csv", "Complex Logic", "Linked by eventId for recurrence."),
                                createFeatureBox("Technical Goals", "Programming Mastery",
                                                "Git, Modularity, Algorithms"));

                VBox rightDS = new VBox();
                Label calDesc = new Label("Calendar Interface\nIntuitive view for all events.");
                calDesc.setWrapText(true);
                rightDS.getChildren().addAll(calDesc);

                dsContainer.getChildren().addAll(leftDS, rightDS);

                Label teamTitle = new Label("PROJECT TEAM");
                teamTitle.getStyleClass().add("section-label");

                HBox teamBox = new HBox(20);
                teamBox.setAlignment(Pos.CENTER);
                teamBox.getChildren().addAll(
                                new Label("Mak Jia Hng"), new Label("Tiew Yi Nuo"),
                                new Label("Fong Jun Toh"), new Label("Tan Ke Qin"), new Label("Ng Peng Han"));

                contentWrapper.getChildren().addAll(
                                heroBox, aboutTitle, missionText, featuresFeatures,
                                dsTitle, dsContainer,
                                teamTitle, teamBox);

                scrollPane.setContent(contentWrapper);

                root.setCenter(scrollPane);

                return root;
        }

        private VBox createCard(String title, String desc) {
                VBox card = new VBox(10);
                card.getStyleClass().add("about-card");
                Label t = new Label(title);
                t.getStyleClass().add("card-title");
                Label d = new Label(desc);
                d.setWrapText(true);
                d.getStyleClass().add("card-desc");
                card.getChildren().addAll(t, d);
                return card;
        }

        private VBox createFeatureBox(String title, String sub, String desc) {
                VBox box = new VBox(5);
                box.getStyleClass().add("feature-box");
                Label t = new Label(title);
                t.setStyle("-fx-font-weight:bold; -fx-font-size:16px;");
                Label s = new Label(sub);
                s.setStyle("-fx-font-weight:bold; -fx-text-fill:#666;");
                Label d = new Label(desc);
                d.setWrapText(true);
                box.getChildren().addAll(t, s, d);
                return box;
        }

        private void showProblemStatement() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Project Objective");
                alert.setHeaderText("Optimize Limited Daytime");
                alert.setContentText(
                                "Core Task: Build a Calendar App using Java (OOP, File I/O).\nConstraint: No databases allowed. Must use local .csv files.");
                alert.showAndWait();
        }
}
