package com.example.frontend.pages;

import com.example.frontend.model.AppUser;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class SettingsPage {

    private final Consumer<String> navigate;
    private final AppUser currentUser;

    public SettingsPage() {
        this(null, null);
    }

    public SettingsPage(Consumer<String> navigate) {
        this(navigate, null);
    }

    public SettingsPage(Consumer<String> navigate, AppUser currentUser) {
        this.navigate = navigate;
        this.currentUser = currentUser;
    }

    public Node getView() {
        VBox root = new VBox(18);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("settings-container");
        root.setMaxWidth(900);

        try {
            root.getStylesheets()
                    .add(getClass().getResource("/frontend/CSS_SubPage/SettingsPage.css").toExternalForm());
        } catch (Exception e) {
            // ignore
        }

        if (navigate != null) {
            root.getChildren().add(new com.example.frontend.components.NavBar(navigate));
        }

        Label title = new Label("Settings");
        title.getStyleClass().add("settings-title");

        String subtitleText = "Adjust your preferences and configure the app.";
        if (currentUser != null && currentUser.getName() != null && !currentUser.getName().isBlank()) {
            subtitleText = "Settings for " + currentUser.getName() + ".";
        }

        Label subtitle = new Label(subtitleText);
        subtitle.getStyleClass().add("settings-subtitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(700);

        Label hint = new Label("Tip: Theme colors can be changed from the Calendar sidebar.");
        hint.getStyleClass().add("settings-hint");
        hint.setWrapText(true);
        hint.setMaxWidth(700);

        root.getChildren().addAll(title, subtitle, hint);
        return root;
    }
}
