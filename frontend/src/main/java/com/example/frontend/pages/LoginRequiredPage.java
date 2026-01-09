package com.example.frontend.pages;

import com.example.frontend.context.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class LoginRequiredPage {

    private final Consumer<String> navigate;
    private final String messageText;
    private final String actionRoute;
    private final String actionLabel;

    public LoginRequiredPage(Consumer<String> navigate) {
        this(navigate, "Please log in to continue.", "/login", "Go to Login");
    }

    public LoginRequiredPage(Consumer<String> navigate, String messageText, String actionRoute, String actionLabel) {
        this.navigate = navigate;
        this.messageText = (messageText != null && !messageText.isBlank()) ? messageText : "Please log in to continue.";
        this.actionRoute = (actionRoute != null && !actionRoute.isBlank()) ? actionRoute : "/login";
        this.actionLabel = (actionLabel != null && !actionLabel.isBlank()) ? actionLabel : "Go to Login";
    }

    public Node getView() {
        ThemeManager theme = ThemeManager.getInstance();

        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(30));
        theme.applyBackground(root);

        VBox content = new VBox(16);
        content.setAlignment(Pos.CENTER);

        Label message = new Label(messageText);
        message.setStyle("-fx-font-size: 18px; -fx-text-fill: #555;");

        Button actionBtn = new Button(actionLabel);
        actionBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 30;");
        themeSolidButton(actionBtn, theme.getButtonColor(), theme.getHoverButtonColor());
        actionBtn.setOnAction(e -> {
            if (navigate != null) {
                navigate.accept(actionRoute);
            }
        });

        content.getChildren().addAll(message, actionBtn);

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35));
        card.setMaxWidth(650);
        card.setStyle(
                "-fx-background-color: white;" +
                        " -fx-background-radius: 25;" +
                        " -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.18), 18, 0, 0, 6);");

        if (navigate != null) {
            com.example.frontend.components.NavBar navBar = new com.example.frontend.components.NavBar(navigate);
            navBar.setAlignment(Pos.CENTER);
            card.getChildren().add(navBar);
        }

        card.getChildren().add(content);

        StackPane cardWrap = new StackPane(card);
        cardWrap.setAlignment(Pos.CENTER);
        VBox.setVgrow(cardWrap, javafx.scene.layout.Priority.ALWAYS);

        root.getChildren().add(cardWrap);

        return root;
    }

    private void themeSolidButton(Button button, String normalBg, String hoverBg) {
        if (button == null) {
            return;
        }

        button.setStyle(button.getStyle() + " -fx-background-color: " + normalBg + "; -fx-text-fill: white;");
        button.setOnMouseEntered(e -> button.setStyle(
                button.getStyle().replaceAll("-fx-background-color\\s*:[^;]+;",
                        "-fx-background-color: " + hoverBg + ";")
                        + " -fx-text-fill: white;"));
        button.setOnMouseExited(e -> button.setStyle(
                button.getStyle().replaceAll("-fx-background-color\\s*:[^;]+;",
                        "-fx-background-color: " + normalBg + ";")
                        + " -fx-text-fill: white;"));
    }
}
