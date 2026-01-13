package com.example.frontend;

import com.example.frontend.model.AppUser;
import com.example.frontend.context.ThemeManager;
import com.example.frontend.pages.*;
import com.example.frontend.service.EventCsvService;
import com.example.frontend.service.UserCsvService;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main JavaFX Application class that manages the calendar application.
 * 
 * This class serves as the central controller for the application, handling:
 * - Application initialization and window setup
 * - Route-based navigation between different pages
 * - User session management (login/logout)
 * - Theme application across all pages
 * - Service initialization (UserCsvService, EventCsvService)
 * 
 * The application uses a single Scene with dynamically swapped root nodes
 * for navigation, similar to single-page application routing.
 * 
 * Available routes:
 * - / or /home: Home page (accessible without login)
 * - /login: User login page
 * - /logout: Logout and redirect to login
 * - /calendar: Calendar view page
 * - /create-event: Event creation page (requires login)
 * - /statistics: Statistics and analytics page
 * - /backup-restore: Backup and restore utilities
 * - /about: About page
 * - /settings: User settings page
 */
public class App extends Application {

    /** The main scene used throughout the application lifecycle */
    private static Scene scene;

    /** Currently logged-in user, null if no user is logged in */
    private AppUser currentUser;

    /** Service for managing user data and CSV operations */
    private UserCsvService userService;

    /** Service for managing event data and CSV operations */
    private EventCsvService eventService;

    /**
     * Initializes and starts the JavaFX application.
     * 
     * This method is called by the JavaFX runtime when the application is launched.
     * It performs the following setup:
     * 1. Initializes UserCsvService and EventCsvService
     * 2. Creates a 1200x800 Scene with a StackPane root
     * 3. Sets the window title and application icon
     * 4. Displays the window
     * 5. Navigates to the home route ("/")
     * 
     * @param stage The primary stage for this application
     * @throws IOException If an I/O error occurs during initialization
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize Services
        userService = new UserCsvService();
        eventService = new EventCsvService();

        // Initialize Root
        StackPane root = new StackPane();
        scene = new Scene(root, 1200, 800); // Increased size for desktop feel

        stage.setTitle("Calendar Application");
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/frontend/calendar_app.png")));
        stage.setScene(scene);
        stage.show();

        // Initial Navigation
        navigate("/");
    }

    /**
     * Navigates to a specified route by updating the scene's root node.
     * 
     * This method implements client-side routing by swapping the root node
     * of the application's scene based on the requested route.
     * Each page is wrapped in a themed StackPane before being set as the root.
     * 
     * Authentication is enforced for certain routes (e.g., /create-event).
     * If a user tries to access a protected route without being logged in,
     * they are redirected to a login-required page.
     * 
     * @param route The route path to navigate to (e.g., "/calendar", "/login")
     *              Supported routes: /, /home, /login, /logout, /calendar,
     *              /create-event, /statistics, /backup-restore, /about, /settings
     */
    public void navigate(String route) {
        if (scene == null)
            return;

        ThemeManager theme = ThemeManager.getInstance();

        switch (route) {
            case "/logout":
                // Clear session and redirect to login
                this.currentUser = null;
                navigate("/login");
                break;
            case "/login":
                LoginPage loginPage = new LoginPage(this::navigate, userService, this);
                scene.setRoot(createThemedRoot(loginPage.getView(), theme));
                break;
            case "/":
            case "/home":
                // Home page is now creating accessible without a user
                HomePage homePage = new HomePage(this::navigate, currentUser);
                scene.setRoot(createThemedRoot(homePage.getView(), theme));
                break;
            case "/calendar":
                CalendarPage calendarPage = new CalendarPage(this::navigate, eventService, currentUser);
                scene.setRoot(createThemedRoot(calendarPage.getView(), theme));
                break;
            case "/create-event":
                if (currentUser == null || currentUser.getId() == null || currentUser.getId() <= 0) {
                    LoginRequiredPage loginRequiredPage = new LoginRequiredPage(
                            this::navigate,
                            "Please log in to create events",
                            "/login",
                            "Go to Login");
                    scene.setRoot(createThemedRoot(loginRequiredPage.getView(), theme));
                } else {
                    CreateEventPage createEventPage = new CreateEventPage(eventService, currentUser, this::navigate);
                    scene.setRoot(createThemedRoot(createEventPage.getView(), theme));
                }
                break;
            case "/statistics":
                StatisticsPage statsPage = new StatisticsPage(this::navigate, eventService, currentUser);
                scene.setRoot(createThemedRoot(statsPage.getView(), theme));
                break;
            case "/backup-restore":
                BackupRestorePage backupPage = new BackupRestorePage(this::navigate);
                scene.setRoot(createThemedRoot(backupPage.getView(), theme));
                break;
            case "/about":
                AboutPage aboutPage = new AboutPage(this::navigate);
                scene.setRoot(createThemedRoot(aboutPage.getView(), theme));
                break;
            case "/settings":
                SettingsPage settingsPage = new SettingsPage(this::navigate, currentUser);
                scene.setRoot(createThemedRoot(settingsPage.getView(), theme));
                break;
            default:
                System.out.println("Unknown route: " + route);
                // Fallback to home or login
                if (currentUser != null)
                    navigate("/");
                else
                    navigate("/login");
                break;
        }
    }

    /**
     * Wraps a page view in a themed StackPane container.
     * 
     * This method creates a wrapper StackPane around the provided page view
     * and applies the current theme's background styling to both the wrapper
     * and the page view itself (if it's a Region).
     * 
     * This ensures consistent theming across all pages of the application.
     * 
     * @param pageView The page view (node) to wrap and theme
     * @param theme    The ThemeManager instance containing the current theme
     *                 settings
     * @return A StackPane containing the themed page view
     */
    private StackPane createThemedRoot(Node pageView, ThemeManager theme) {
        StackPane wrapper = new StackPane(pageView);
        theme.applyBackground(wrapper);
        if (pageView instanceof Region region) {
            theme.applyBackground(region);
        }
        return wrapper;
    }

    /**
     * Main entry point for the JavaFX application.
     * 
     * Note: In production, {@link Launcher#main(String[])} should be used
     * as the entry point to avoid JavaFX module system issues.
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Sets the currently logged-in user.
     * 
     * This method is typically called by the LoginPage after successful
     * authentication.
     * Setting the user to null effectively logs the user out.
     * 
     * @param user The user to set as the current user, or null to clear the session
     */
    public void setCurrentUser(AppUser user) {
        this.currentUser = user;
    }

    /**
     * Retrieves the currently logged-in user.
     * 
     * @return The current AppUser instance, or null if no user is logged in
     */
    public AppUser getCurrentUser() {
        return currentUser;
    }

}