package com.example.frontend;

import com.example.frontend.model.AppUser;
import com.example.frontend.context.ThemeManager;
import com.example.frontend.pages.*;
import com.example.frontend.service.EventCsvService;
import com.example.frontend.service.UserCsvService;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private AppUser currentUser;
    private UserCsvService userService;
    private EventCsvService eventService;

    @Override
    public void start(Stage stage) throws IOException {
        // Initialize Services
        userService = new UserCsvService();
        eventService = new EventCsvService();

        // Initialize Root
        StackPane root = new StackPane();
        scene = new Scene(root, 1200, 800); // Increased size for desktop feel

        stage.setTitle("Calendar Application");
        stage.setScene(scene);
        stage.show();

        // Initial Navigation
        navigate("/");
    }

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
                CreateEventPage createEventPage = new CreateEventPage(eventService, currentUser, this::navigate);
                scene.setRoot(createThemedRoot(createEventPage.getView(), theme));
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

    private StackPane createThemedRoot(Node pageView, ThemeManager theme) {
        StackPane wrapper = new StackPane(pageView);
        theme.applyBackground(wrapper);
        if (pageView instanceof Region region) {
            theme.applyBackground(region);
        }
        return wrapper;
    }

    public static void main(String[] args) {
        launch();
    }

    public void setCurrentUser(AppUser user) {
        this.currentUser = user;
    }

    public AppUser getCurrentUser() {
        return currentUser;
    }

}