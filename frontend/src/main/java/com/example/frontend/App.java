package com.example.frontend;

import com.example.frontend.model.AppUser;
import com.example.frontend.pages.*;
import com.example.frontend.service.EventCsvService;
import com.example.frontend.service.UserCsvService;
import javafx.application.Application;
import javafx.scene.Scene;
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

        switch (route) {
            case "/logout":
                // Clear session and redirect to login
                this.currentUser = null;
                navigate("/login");
                break;
            case "/login":
                LoginPage loginPage = new LoginPage(this::navigate, userService, this);
                scene.setRoot(new StackPane(loginPage.getView()));
                break;
            case "/":
            case "/home":
                // Home page is now creating accessible without a user
                HomePage homePage = new HomePage(this::navigate, currentUser);
                scene.setRoot(new StackPane(homePage.getView()));
                break;
            case "/calendar":
                CalendarPage calendarPage = new CalendarPage(this::navigate, eventService, currentUser);
                scene.setRoot(new StackPane(calendarPage.getView()));
                break;
            case "/create-event":
                CreateEventPage createEventPage = new CreateEventPage(eventService, currentUser, this::navigate);
                scene.setRoot(new StackPane(createEventPage.getView()));
                break;
            case "/statistics":
                StatisticsPage statsPage = new StatisticsPage(this::navigate);
                scene.setRoot(new StackPane(statsPage.getView()));
                break;
            case "/backup-restore":
                BackupRestorePage backupPage = new BackupRestorePage(this::navigate);
                scene.setRoot(new StackPane(backupPage.getView()));
                break;
            case "/about":
                AboutPage aboutPage = new AboutPage(this::navigate);
                scene.setRoot(new StackPane(aboutPage.getView()));
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