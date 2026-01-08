package com.example.frontend.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import java.util.function.Consumer;

public class NavBar extends HBox {

    private final Consumer<String> navigate;

    public NavBar(Consumer<String> navigate) {
        this.navigate = navigate;

        this.setSpacing(15);
        this.setPadding(new Insets(10));
        this.setAlignment(Pos.CENTER_LEFT);
        this.getStyleClass().add("nav-bar");

        // Ensure it picks up mouse events and doesn't get blocked if overlaid
        this.setPickOnBounds(false);

        // Buttons
        Button homeBtn = createNavButton("🏠", "/");
        Button calBtn = createNavButton("📅", "/calendar");
        Button loginBtn = createNavButton("🔒", "/login");
        Button statsBtn = createNavButton("📊", "/statistics");

        this.getChildren().addAll(homeBtn, calBtn, loginBtn, statsBtn);
    }

    private Button createNavButton(String text, String route) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-icon-btn");
        btn.setStyle("-fx-font-size: 20px; -fx-background-color: transparent; -fx-cursor: hand;");
        btn.setOnAction(e -> {
            if (navigate != null) {
                navigate.accept(route);
            }
        });
        return btn;
    }
}
