package com.example.frontend.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;
import com.example.frontend.model.AppUser;

/**
 * HomePage serves as the main dashboard of the application.
 * Displays a welcome message, user information, and a grid of navigation cards
 * that allow users to access different features of the application.
 * 
 * The page features a card-based interface with links to Calendar, Login,
 * About,
 * Statistics, Settings, and Backup & Restore pages.
 */
public class HomePage {
    private final Consumer<String> navigate;
    private final AppUser user;

    /**
     * Constructs a HomePage with navigation callback and user information.
     * 
     * @param navigate A callback function for navigating between pages in the
     *                 application
     * @param user     The currently logged-in user, or null if no user is logged
     *                 in.
     *                 Used to display a personalized welcome message and logout
     *                 option.
     */
    public HomePage(Consumer<String> navigate, AppUser user) {
        this.navigate = navigate;
        this.user = user;
    }

    /**
     * Creates and returns the main view of the home page.
     * 
     * The view consists of:
     * - A themed background container
     * - A centered white card with rounded corners and shadow
     * - Navigation bar at the top
     * - Welcome header with app title and subtitle
     * - User welcome section (if user is logged in) with logout button
     * - 3x2 grid of feature cards for navigation to different sections
     * 
     * The cards are arranged in a fixed grid:
     * Row 1: Calendar, Login, About
     * Row 2: Statistics, Settings, Backup & Restore
     * 
     * Loads and applies CSS styling from HomePage.css.
     * 
     * @return A JavaFX Node containing the complete home page UI
     */
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
        GridPane cardsContainer = new GridPane();
        cardsContainer.setHgap(24);
        cardsContainer.setVgap(24);
        cardsContainer.setAlignment(Pos.CENTER);
        cardsContainer.getStyleClass().add("home-cards");

        // Fixed 3x2 matrix
        addCard(cardsContainer, 0, "Calendar", "Check your schedule and plan your days efficiently.", "/calendar");
        addCard(cardsContainer, 1, "Login", "Sign in or create a new account to access your dashboard.", "/login");
        addCard(cardsContainer, 2, "About", "Learn more about the features of this application.", "/about");
        addCard(cardsContainer, 3, "Statistics", "View and update your personal information.", "/statistics");
        addCard(cardsContainer, 4, "Settings", "Adjust your preferences and configure the app.", "/settings");
        addCard(cardsContainer, 5, "Backup & Restore", "Backup your calendar or Restore calendar.", "/backup-restore");

        centerContent.getChildren().addAll(headerBox, userSection, cardsContainer);
        mainCard.setCenter(centerContent);

        root.getChildren().add(mainCard);
        return root;
    }

    /**
     * Creates the navigation bar component for the home page.
     * 
     * @return An HBox containing the NavBar component with navigation capabilities
     */
    private HBox createNavBar() {
        return new com.example.frontend.components.NavBar(navigate);
    }

    /**
     * Creates and adds a navigation card to the grid layout.
     * 
     * Each card displays a title and description, and navigates to the specified
     * route when clicked. Cards are styled with CSS and positioned in a 3-column
     * grid.
     * 
     * @param parent The GridPane container to add the card to
     * @param index  The position index (0-5) determining the card's grid location.
     *               Index is converted to row/column: col = index % 3, row = index
     *               / 3
     * @param title  The title text displayed on the card
     * @param desc   The description text displayed below the title (supports text
     *               wrapping)
     * @param route  The navigation route to go to when the card is clicked (e.g.,
     *               "/calendar")
     */
    private void addCard(GridPane parent, int index, String title, String desc, String route) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setOnMouseClicked(e -> navigate.accept(route));

        Label t = new Label(title);
        t.getStyleClass().add("card-title");

        Label d = new Label(desc);
        d.setWrapText(true);
        d.getStyleClass().add("card-desc");

        card.getChildren().addAll(t, d);
        int col = index % 3;
        int row = index / 3;
        parent.add(card, col, row);
    }

    /**
     * Handles user logout by navigating to the logout route.
     * This triggers the logout process and redirects to the logout page.
     */
    private void handleLogout() {
        navigate.accept("/logout");
    }
}
