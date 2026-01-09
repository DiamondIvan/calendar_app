package com.example.frontend.pages;

import com.example.frontend.App;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.function.Consumer;
import com.example.frontend.service.UserCsvService;
import com.example.frontend.model.AppUser;

public class LoginPage {

    private final Consumer<String> navigate;
    private final UserCsvService userService;
    private final App app;

    private StackPane overlayPane;
    private VBox overlayContentLeft; // "Welcome Back"
    private VBox overlayContentRight; // "Hello Friend"

    private VBox signInForm;
    private VBox signUpForm;

    // Config
    private final double CONTAINER_WIDTH = 900;
    private final double CONTAINER_HEIGHT = 600;
    private final double OVERLAY_WIDTH = CONTAINER_WIDTH * 0.4; // 40% width

    private boolean isSignInActive = true;

    public LoginPage(Consumer<String> navigate, UserCsvService userService, App app) {
        this.navigate = navigate;
        this.userService = userService;
        this.app = app;
    }

    public Node getView() {
        // Root Container that holds the centered card
        BorderPane outerRoot = new BorderPane();
        outerRoot.getStyleClass().add("outer-root");

        try {
            outerRoot.getStylesheets()
                    .add(getClass().getResource("/frontend/CSS_SubPage/LoginPage.css").toExternalForm());
        } catch (Exception e) {
        }

        // 1. The Main Card Container (Relative)
        Pane mainContainer = new Pane();
        mainContainer.getStyleClass().add("main-container");
        mainContainer.setPrefSize(CONTAINER_WIDTH, CONTAINER_HEIGHT);
        mainContainer.setMaxSize(CONTAINER_WIDTH, CONTAINER_HEIGHT);

        // 2. Forms Layer
        // Sign In Form (Left side)
        signInForm = createSignInForm();
        signInForm.setPrefSize(CONTAINER_WIDTH * 0.6, CONTAINER_HEIGHT); // 60% Width
        signInForm.setLayoutX(0); // Left aligned
        signInForm.setLayoutY(0);

        // Sign Up Form (Right side)
        signUpForm = createSignUpForm();
        signUpForm.setPrefSize(CONTAINER_WIDTH * 0.6, CONTAINER_HEIGHT); // 60% Width
        // Initially, Sign Up form is "hidden" by the overlay which is on the right
        signUpForm.setLayoutX(CONTAINER_WIDTH * 0.4);
        signUpForm.setLayoutY(0);

        // 3. Overlay Layer (The sliding green panel)
        overlayPane = new StackPane();
        overlayPane.getStyleClass().add("overlay-pane");
        overlayPane.setPrefSize(OVERLAY_WIDTH, CONTAINER_HEIGHT);

        overlayPane.setLayoutX(CONTAINER_WIDTH - OVERLAY_WIDTH); // Start at Right
        overlayPane.setLayoutY(0);

        // Overlay Content
        overlayContentRight = createOverlayContent("Hello, Friend!",
                "Register with your personal details to use all of site features", "SIGN UP", this::toggleMode);
        overlayContentLeft = createOverlayContent("Welcome Back!",
                "Enter your personal details to use all of site features", "SIGN IN", this::toggleMode);

        // Initially, we are in Sign In Mode -> Overlay is on Right -> Show "Hello
        // Friend (Sign Up)"
        overlayContentRight.setVisible(true);
        overlayContentLeft.setVisible(false);

        overlayPane.getChildren().addAll(overlayContentLeft, overlayContentRight);

        // Add everything to container
        double formWidth = CONTAINER_WIDTH - OVERLAY_WIDTH; // 900 - 360 = 540
        signInForm.setPrefWidth(formWidth);
        signUpForm.setPrefWidth(formWidth);

        signUpForm.setLayoutX(OVERLAY_WIDTH);

        mainContainer.getChildren().addAll(signInForm, signUpForm, overlayPane);

        // Ensure visibility logic
        signUpForm.setVisible(false); // Optimization: Hide what's covered
        signInForm.setVisible(true);

        // --- NavBar (aligned with left side of signin container) ---
        com.example.frontend.components.NavBar navBar = new com.example.frontend.components.NavBar(navigate);
        HBox navBarContainer = new HBox(navBar);
        navBarContainer.setAlignment(Pos.CENTER_LEFT);
        navBarContainer.setPrefWidth(CONTAINER_WIDTH);
        navBarContainer.setPadding(new Insets(0, 0, 0, 165)); // top, right, bottom, left - adjust as needed

        // Wrapper with NavBar above the main container
        VBox contentWithNav = new VBox(10);
        contentWithNav.setAlignment(Pos.CENTER);
        contentWithNav.getChildren().addAll(navBarContainer, mainContainer);

        StackPane centerWrapper = new StackPane(contentWithNav);
        centerWrapper.setAlignment(Pos.CENTER);

        outerRoot.setCenter(centerWrapper);

        return outerRoot;
    }

    private void toggleMode() {
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), overlayPane);

        if (isSignInActive) {
            // Variable Change
            isSignInActive = false;

            // Move Overlay to Left
            transition.setToX(-(CONTAINER_WIDTH - OVERLAY_WIDTH));

            transition.setOnFinished(e -> {
                // Update internal state if needed
                overlayPane.setLayoutX(0);
                overlayPane.setTranslateX(0);
            });

            // Swap Content Visibility
            overlayContentRight.setVisible(false);
            overlayContentLeft.setVisible(true);

            signInForm.setVisible(false);
            signUpForm.setVisible(true);

        } else {
            // Switch to Sign In Mode
            isSignInActive = true;

            // Move Overlay to Right
            transition.setToX(CONTAINER_WIDTH - OVERLAY_WIDTH);

            transition.setOnFinished(e -> {
                overlayPane.setLayoutX(CONTAINER_WIDTH - OVERLAY_WIDTH);
                overlayPane.setTranslateX(0);
            });

            overlayContentRight.setVisible(true);
            overlayContentLeft.setVisible(false);

            signInForm.setVisible(true);
            signUpForm.setVisible(false);
        }

        transition.play();
    }

    private VBox createSignInForm() {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.getStyleClass().add("form-container");

        Label title = new Label("Sign In");
        title.getStyleClass().add("form-title");

        HBox socialBox = createSocialIcons();

        Label orLabel = new Label("or use your email password");
        orLabel.getStyleClass().add("sub-text");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("input-field");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.getStyleClass().add("input-field");

        Label forgot = new Label("Forget Your Password?");
        forgot.getStyleClass().add("forgot-text");

        Button signInBtn = new Button("SIGN IN");
        signInBtn.getStyleClass().add("action-btn");
        signInBtn.setOnAction(e -> handleSignIn(emailField.getText(), passField.getText()));

        // Allow submitting with Enter key
        passField.setOnAction(e -> handleSignIn(emailField.getText(), passField.getText()));

        box.getChildren().addAll(title, socialBox, orLabel, emailField, passField, forgot, signInBtn);
        return box;
    }

    private VBox createSignUpForm() {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.getStyleClass().add("form-container");

        Label title = new Label("Create Account");
        title.getStyleClass().add("form-title");

        HBox socialBox = createSocialIcons();

        Label orLabel = new Label("or use your email for registration");
        orLabel.getStyleClass().add("sub-text");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        nameField.getStyleClass().add("input-field");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("input-field");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.getStyleClass().add("input-field");

        Button signUpBtn = new Button("SIGN UP");
        signUpBtn.getStyleClass().add("action-btn");
        signUpBtn.setOnAction(e -> handleSignUp(nameField.getText(), emailField.getText(), passField.getText()));

        // Allow submitting with Enter key
        passField.setOnAction(e -> handleSignUp(nameField.getText(), emailField.getText(), passField.getText()));

        box.getChildren().addAll(title, socialBox, orLabel, nameField, emailField, passField, signUpBtn);
        return box;
    }

    private VBox createOverlayContent(String titleText, String descText, String btnText, Runnable action) {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.getStyleClass().add("overlay-content");

        Label title = new Label(titleText);
        title.getStyleClass().add("overlay-title");

        Label desc = new Label(descText);
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);
        desc.getStyleClass().add("overlay-desc");

        Button btn = new Button(btnText);
        btn.getStyleClass().add("overlay-btn");
        btn.setOnAction(e -> action.run());

        box.getChildren().addAll(title, desc, btn);
        return box;
    }

    private HBox createSocialIcons() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);
        // Mock buttons for G+, FB, Github, Linkedin
        String[] labels = { "G+", "f", "In", "Li" };
        for (String l : labels) {
            Button b = new Button(l);
            b.getStyleClass().add("social-icon-btn");
            box.getChildren().add(b);
        }
        return box;
    }

    private void handleSignIn(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }

        AppUser user = userService.validateUser(email, password);
        if (user != null) {
            System.out.println("Login Successful: " + user.getName());
            app.setCurrentUser(user);
            navigate.accept("/");
        } else {
            showAlert("Login Failed", "Invalid email or password");
        }
    }

    private void handleSignUp(String name, String email, String password) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }

        if (userService.emailExists(email)) {
            showAlert("Error", "Email already registered");
            return;
        }

        AppUser newUser = new AppUser();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);

        userService.saveUser(newUser);
        showAlert("Success", "Account created! Please sign in.");
        // Auto-switch to login mode?
        // We are currently in Sign Up mode (Overlay on Left).
        // Trigger toggle.
        toggleMode();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
