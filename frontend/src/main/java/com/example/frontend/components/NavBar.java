package com.example.frontend.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import java.io.InputStream;
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
        Button homeBtn = createNavButton("home.png", "/");
        Button calBtn = createNavButton("calender1.png", "/calendar");
        Button loginBtn = createNavButton("Login1.png", "/login");
        Button statsBtn = createNavButton("bar-chart.png", "/statistics");
        Button settingsBtn = createNavButton("settings.png", "/settings");

        this.getChildren().addAll(homeBtn, calBtn, loginBtn, statsBtn, settingsBtn);
    }

    private Button createNavButton(String iconFileName, String route) {
        Button btn = new Button();
        btn.getStyleClass().add("nav-icon-btn");
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        // Load icon image
        try {
            InputStream is = getClass().getResourceAsStream("/frontend/public/" + iconFileName);
            if (is != null) {
                Image img = new Image(is);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(40);
                iv.setFitHeight(40);
                iv.setPreserveRatio(true);
                btn.setGraphic(iv);
            }
        } catch (Exception e) {
            // Fallback to text if image not found
            btn.setText("•");
        }

        btn.setOnAction(e -> {
            if (navigate != null) {
                navigate.accept(route);
            }
        });
        return btn;
    }
}
