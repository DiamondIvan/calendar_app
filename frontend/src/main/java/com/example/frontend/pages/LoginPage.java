package com.example.frontend.pages;

import com.example.frontend.App;
import com.example.frontend.context.ThemeManager;
import javafx.animation.TranslateTransition;
import javafx.animation.Interpolator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.function.Consumer;
import com.example.frontend.service.UserCsvService;
import com.example.frontend.model.AppUser;

/**
 * LoginPage provides an animated authentication interface with sliding
 * transitions.
 * 
 * Features a modern sliding overlay design that switches between Sign In and
 * Sign Up modes:
 * - Animated overlay panel that slides left/right with smooth transitions
 * - Dual-mode interface: Sign In (default) and Sign Up
 * - Theme-aware color scheme that adapts to calendar theme selection
 * - Social login placeholders (Google+, Facebook, LinkedIn, etc.)
 * - Form validation and user feedback via alerts
 * 
 * Architecture:
 * - Fixed 900x600 container with forms taking 60% width and overlay 45% width
 * - Forms positioned on left/right, overlay slides to reveal active form
 * - Uses JavaFX TranslateTransition for smooth 650ms animations
 * - Integrates with UserCsvService for authentication and registration
 * 
 * The page automatically applies colors based on the calendar theme selection,
 * ensuring visual consistency across the application.
 */
public class LoginPage {

    private final Consumer<String> navigate;
    private final UserCsvService userService;
    private final App app;

    private StackPane overlayPane;
    private VBox overlayContentLeft; // "Welcome Back"
    private VBox overlayContentRight; // "Hello Friend"

    private VBox signInForm;
    private VBox signUpForm;

    private Button signInBtn;
    private Button signUpBtn;

    // Config
    private final double CONTAINER_WIDTH = 900;
    private final double CONTAINER_HEIGHT = 600;
    private final double OVERLAY_WIDTH = CONTAINER_WIDTH * 0.45; // 45% width

    private boolean isSignInActive = true;
    private boolean isAnimating = false;

    /**
     * Constructs a LoginPage with navigation, user service, and app reference.
     * 
     * @param navigate    Callback function for navigating between pages
     * @param userService Service for user authentication and registration
     *                    operations
     * @param app         Reference to the main App instance for setting the current
     *                    user after login
     */
    public LoginPage(Consumer<String> navigate, UserCsvService userService, App app) {
        this.navigate = navigate;
        this.userService = userService;
        this.app = app;
    }

    /**
     * Creates and returns the complete login page view with sliding animation.
     * 
     * The view structure:
     * 1. Outer BorderPane with themed background
     * 2. Navigation bar centered at top
     * 3. Main container (900x600) with three layers:
     * - Sign In form (left, initially visible)
     * - Sign Up form (right, initially hidden)
     * - Sliding overlay panel (starts at right, slides to reveal forms)
     * 
     * The overlay contains two states:
     * - Right position: Shows "Hello, Friend!" to encourage sign up
     * - Left position: Shows "Welcome Back!" to encourage sign in
     * 
     * Loads LoginPage.css for styling and applies theme colors.
     * 
     * @return A JavaFX Node containing the complete animated login interface
     */
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

        // Keep layout fixed and animate via translateX for smoother transitions.
        overlayPane.setLayoutX(0);
        overlayPane.setLayoutY(0);
        overlayPane.setTranslateX(CONTAINER_WIDTH - OVERLAY_WIDTH); // Start at Right

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

        // --- NavBar (centered above the login card) ---
        com.example.frontend.components.NavBar navBar = new com.example.frontend.components.NavBar(navigate);
        // NavBar defaults to CENTER_LEFT; on the login page we want the icons centered.
        navBar.setAlignment(Pos.CENTER);
        StackPane navBarContainer = new StackPane(navBar);
        navBarContainer.setPrefWidth(CONTAINER_WIDTH);
        StackPane.setAlignment(navBar, Pos.CENTER);

        // Wrapper with NavBar above the main container
        VBox contentWithNav = new VBox(10);
        contentWithNav.setAlignment(Pos.CENTER);
        contentWithNav.getChildren().addAll(navBarContainer, mainContainer);

        StackPane centerWrapper = new StackPane(contentWithNav);
        centerWrapper.setAlignment(Pos.CENTER);

        outerRoot.setCenter(centerWrapper);

        applyLoginScheme(outerRoot);

        return outerRoot;
    }

    /**
     * Applies theme-aware color scheme to the login page.
     * 
     * Uses ThemeManager to:
     * - Apply background gradient matching the calendar theme
     * - Get primary and secondary colors based on sidebar color selection
     * - Style the overlay panel with gradient from primary to secondary
     * - Apply colors to Sign In and Sign Up buttons
     * 
     * The color scheme automatically updates when the calendar sidebar color
     * changes,
     * providing visual cohesion across the application.
     * 
     * @param outerRoot The root BorderPane to apply the background theme to
     */
    private void applyLoginScheme(BorderPane outerRoot) {
        ThemeManager themeManager = ThemeManager.getInstance();

        // Keep global background consistent with the selected Calendar theme
        themeManager.applyBackground(outerRoot);

        String[] loginColors = themeManager.getLoginPageColors();
        String primary = loginColors[0];
        String secondary = loginColors[1];

        if (overlayPane != null) {
            overlayPane.setStyle("-fx-background-color: linear-gradient(to bottom right, " + primary + ", " + secondary
                    + ");");
        }

        themeSolidButton(signInBtn, primary, secondary);
        themeSolidButton(signUpBtn, primary, secondary);
    }

    /**
     * Applies themed solid colors to a button with hover effects.
     * 
     * @param button   The button to style (if null, method returns immediately)
     * @param normalBg Normal state background color
     * @param hoverBg  Hover state background color
     */
    private void themeSolidButton(Button button, String normalBg, String hoverBg) {
        if (button == null) {
            return;
        }

        button.setStyle("-fx-background-color: " + normalBg + "; -fx-text-fill: white;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + hoverBg + "; -fx-text-fill: white;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + normalBg + "; -fx-text-fill: white;"));
    }

    /**
     * Toggles between Sign In and Sign Up modes with animated transition.
     * 
     * Animation behavior:
     * - Sign In → Sign Up: Overlay slides from right to left (650ms)
     * - Sign Up → Sign In: Overlay slides from left to right (650ms)
     * - Uses EASE_BOTH interpolator for smooth acceleration/deceleration
     * - Prevents multiple simultaneous animations with isAnimating flag
     * - Hides overlay text during transition, shows appropriate text on finish
     * - Toggles form visibility to match the active mode
     * 
     * Visual states:
     * - Sign In active: Overlay on right, shows Sign In form, "Hello Friend"
     * overlay text
     * - Sign Up active: Overlay on left, shows Sign Up form, "Welcome Back" overlay
     * text
     */
    private void toggleMode() {
        if (isAnimating) {
            return;
        }
        isAnimating = true;

        TranslateTransition transition = new TranslateTransition(Duration.millis(650), overlayPane);
        transition.setInterpolator(Interpolator.EASE_BOTH);

        if (isSignInActive) {
            // Variable Change
            isSignInActive = false;

            // Move Overlay to Left
            transition.setToX(0);

            // Hide overlay text during transition; show the new state on finish
            overlayContentRight.setVisible(false);
            overlayContentLeft.setVisible(false);

            transition.setOnFinished(e -> {
                overlayContentRight.setVisible(false);
                overlayContentLeft.setVisible(true);
                signInForm.setVisible(false);
                signUpForm.setVisible(true);
                isAnimating = false;
            });

        } else {
            // Switch to Sign In Mode
            isSignInActive = true;

            // Move Overlay to Right
            transition.setToX(CONTAINER_WIDTH - OVERLAY_WIDTH);

            // Hide overlay text during transition; show the new state on finish
            overlayContentRight.setVisible(false);
            overlayContentLeft.setVisible(false);

            transition.setOnFinished(e -> {
                overlayContentRight.setVisible(true);
                overlayContentLeft.setVisible(false);
                signInForm.setVisible(true);
                signUpForm.setVisible(false);
                isAnimating = false;
            });
        }

        transition.play();
    }

    /**
     * Creates the Sign In form with email and password fields.
     * 
     * Form includes:
     * - "Sign In" title
     * - Social login icons (placeholder buttons)
     * - Email and password input fields
     * - "Forget Your Password?" label
     * - Sign In button that triggers authentication
     * - Enter key support for form submission
     * 
     * @return VBox containing the complete Sign In form
     */
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

        signInBtn = new Button("SIGN IN");
        signInBtn.getStyleClass().add("action-btn");
        signInBtn.setOnAction(e -> handleSignIn(emailField.getText(), passField.getText()));

        // Allow submitting with Enter key
        passField.setOnAction(e -> handleSignIn(emailField.getText(), passField.getText()));

        box.getChildren().addAll(title, socialBox, orLabel, emailField, passField, forgot, signInBtn);
        return box;
    }

    /**
     * Creates the Sign Up form with name, email, and password fields.
     * 
     * Form includes:
     * - "Create Account" title
     * - Social login icons (placeholder buttons)
     * - Name, email, and password input fields
     * - Sign Up button that triggers registration
     * - Enter key support for form submission
     * 
     * @return VBox containing the complete Sign Up form
     */
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

        signUpBtn = new Button("SIGN UP");
        signUpBtn.getStyleClass().add("action-btn");
        signUpBtn.setOnAction(e -> handleSignUp(nameField.getText(), emailField.getText(), passField.getText()));

        // Allow submitting with Enter key
        passField.setOnAction(e -> handleSignUp(nameField.getText(), emailField.getText(), passField.getText()));

        box.getChildren().addAll(title, socialBox, orLabel, nameField, emailField, passField, signUpBtn);
        return box;
    }

    /**
     * Creates overlay content panel for the sliding animation.
     * 
     * The overlay displays contextual messaging to encourage mode switching.
     * Each overlay panel contains a title, description, and action button.
     * 
     * @param titleText Main heading text (e.g., "Welcome Back!")
     * @param descText  Description text explaining what to do
     * @param btnText   Button label (e.g., "SIGN IN", "SIGN UP")
     * @param action    Runnable to execute when button is clicked (typically
     *                  toggleMode)
     * @return VBox containing the overlay content
     */
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

    /**
     * Creates a row of social login icon buttons.
     * 
     * Currently displays placeholder buttons for:
     * - G+ (Google Plus)
     * - f (Facebook)
     * - In (Instagram)
     * - Li (LinkedIn)
     * 
     * These are non-functional placeholders for future social login integration.
     * 
     * @return HBox containing social login icon buttons
     */
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

    /**
     * Handles user sign-in authentication.
     * 
     * Process:
     * 1. Validates that email and password are not empty
     * 2. Calls userService to validate credentials
     * 3. If valid: sets current user in app and navigates to home page
     * 4. If invalid: shows error alert
     * 
     * @param email    The user's email address
     * @param password The user's password
     */
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

    /**
     * Handles new user registration.
     * 
     * Process:
     * 1. Validates that all fields (name, email, password) are filled
     * 2. Checks if email is already registered
     * 3. Creates new AppUser and saves to userService
     * 4. Shows success message
     * 5. Automatically toggles to Sign In mode for the user to log in
     * 
     * @param name     The user's display name
     * @param email    The user's email address
     * @param password The user's chosen password
     */
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

    /**
     * Displays an informational alert dialog to the user.
     * 
     * @param title   The alert title
     * @param content The alert message content
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
