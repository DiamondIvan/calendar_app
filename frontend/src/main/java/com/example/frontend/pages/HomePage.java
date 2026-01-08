package com.example.frontend.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;
import com.example.frontend.model.AppUser;

public class HomePage {
    private final Consumer<String> navigate;
    private final AppUser user;

    public HomePage(Consumer<String> navigate, AppUser user) {
        this.navigate = navigate;
        this.user = user;
    }

    public Node getView() {
        // 1. Root Container (Green Background)
        StackPane root = new StackPane();
        root.getStyleClass().add("root-container");

        // Load CSS
        try {
            root.getStylesheets().add(getClass().getResource("/frontend/CSS_SubPage/HomePage.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Could not load HomePage.css");
        }

        // 2. Main White Card (Rounded, Centered)
        BorderPane mainCard = new BorderPane();
        mainCard.getStyleClass().add("main-white-card");

        // --- Top Navigation Bar ---
        HBox navBar = createNavBar();
        mainCard.setTop(navBar);

        // --- Center Content ---
        VBox centerContent = new VBox(20);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(20));

        // Header Text
        VBox headerBox = new VBox(5);
        headerBox.setAlignment(Pos.CENTER);

        Label title = new Label("Welcome to My App");
        title.getStyleClass().add("home-title");

        Label subtitle = new Label("Your personal dashboard and calendar app");
        subtitle.getStyleClass().add("home-subtitle");

        headerBox.getChildren().addAll(title, subtitle);

        // User Welcome & Logout
        VBox userSection = new VBox(10);
        userSection.setAlignment(Pos.CENTER);

        if (user != null) {
            Label welcome = new Label("Welcome, " + user.getName());
            welcome.getStyleClass().add("user-name");

            Button logoutBtn = new Button("Log Out");
            logoutBtn.getStyleClass().add("logout-btn");
            logoutBtn.setOnAction(e -> handleLogout());

            userSection.getChildren().addAll(welcome, logoutBtn);
        }

        // Cards Grid
        FlowPane cardsContainer = new FlowPane();
        cardsContainer.setHgap(30);
        cardsContainer.setVgap(30);
        cardsContainer.setAlignment(Pos.CENTER);
        cardsContainer.getStyleClass().add("home-cards");

        // Add cards with matching descriptions
        addCard(cardsContainer, "Calendar", "Check your schedule and plan your days efficiently.", "/calendar");
        addCard(cardsContainer, "Login", "Sign in or create a new account to access your dashboard.", "/login");
        addCard(cardsContainer, "About", "Learn more about the features of this application.", "/about");
        addCard(cardsContainer, "Statistics", "View and update your personal information.", "/statistics");
        addCard(cardsContainer, "Settings", "Adjust your preferences and configure the app.", "/settings");
        addCard(cardsContainer, "Backup & Restore", "Backup your calendar or Restore calendar.", "/backup-restore");

        centerContent.getChildren().addAll(headerBox, userSection, cardsContainer);
        mainCard.setCenter(centerContent);

        root.getChildren().add(mainCard);
        return root;
    }

    private HBox createNavBar() {
        return new com.example.frontend.components.NavBar(navigate);
    }

    private void addCard(FlowPane parent, String title, String desc, String route) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setOnMouseClicked(e -> navigate.accept(route));

        Label t = new Label(title);
        t.getStyleClass().add("card-title");

        Label d = new Label(desc);
        d.setWrapText(true);
        d.getStyleClass().add("card-desc");

        card.getChildren().addAll(t, d);
        parent.getChildren().add(card);
    }

    private void handleLogout() {
        navigate.accept("/logout");
    }
}
