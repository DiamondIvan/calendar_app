package com.example.backend.controllers;

import com.example.backend.model.AppUser;
import com.example.backend.service.EventCsvService;
import com.example.backend.service.RecurrentCsvService;
import com.example.backend.service.UserCsvService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for user account management.
 * 
 * Provides HTTP endpoints for:
 * - User registration and login authentication
 * - Email existence checking
 * - User CRUD operations
 * - Cascade deletion (user + events + recurrence rules)
 * 
 * Base URL: /api/users
 * 
 * Security Note: Passwords are stored in plain text (NOT production-ready).
 * For production, implement proper password hashing (bcrypt, etc.).
 * 
 * Uses UserCsvService, EventCsvService, and RecurrentCsvService.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /** Service handling user data operations */
    private final UserCsvService userService;

    /** Service for managing user's events (needed for cascade delete) */
    private final EventCsvService eventService;

    /** Service for managing recurrence rules (needed for cascade delete) */
    private final RecurrentCsvService recurrentService;

    /**
     * Constructs a UserController with new service instances.
     */
    public UserController() {
        this.userService = new UserCsvService();
        this.eventService = new EventCsvService();
        this.recurrentService = new RecurrentCsvService();
    }

    /**
     * Retrieves all users.
     * 
     * Warning: Returns all user data including passwords (not secure).
     * 
     * Response (200 OK): List of all AppUser objects
     * Response (500): Empty response on error
     * 
     * @return ResponseEntity containing list of all users
     */
    @GetMapping
    public ResponseEntity<List<AppUser>> getAllUsers() {
        try {
            List<AppUser> users = userService.loadUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Registers a new user account.
     * 
     * Request body: AppUser object
     * 
     * Validation:
     * - email: Required, non-empty, unique
     * - password: Required, non-empty
     * 
     * ID is auto-generated during save.
     * 
     * Response (201 Created):
     * - success: true
     * - message: "User registered successfully"
     * - user: Created AppUser object with assigned ID
     * 
     * Response (400 Bad Request):
     * - success: false
     * - message: "Email is required" or "Password is required"
     * 
     * Response (409 Conflict):
     * - success: false
     * - message: "Email already exists"
     * 
     * Response (500):
     * - success: false
     * - message: Error description
     * 
     * @param user AppUser object to register
     * @return ResponseEntity with created user or error
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody AppUser user) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                response.put("success", false);
                response.put("message", "Password is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (userService.emailExists(user.getEmail())) {
                response.put("success", false);
                response.put("message", "Email already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            userService.saveUser(user);
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("user", user);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error registering user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Authenticates a user login attempt.
     * 
     * Request body: AppUser object with email and password
     * 
     * Performs exact match on email and password (plain text comparison).
     * 
     * Response (200 OK):
     * - success: true
     * - message: "Login successful"
     * - user: AppUser object (with ID, name, email, password)
     * 
     * Response (400 Bad Request):
     * - success: false
     * - message: "Email and password are required"
     * 
     * Response (401 Unauthorized):
     * - success: false
     * - message: "Invalid email or password"
     * 
     * Response (500):
     * - success: false
     * - message: Error description
     * 
     * @param credentials AppUser object containing email and password
     * @return ResponseEntity with user data or error
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody AppUser credentials) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (credentials.getEmail() == null || credentials.getPassword() == null) {
                response.put("success", false);
                response.put("message", "Email and password are required");
                return ResponseEntity.badRequest().body(response);
            }

            AppUser user = userService.validateUser(credentials.getEmail(), credentials.getPassword());

            if (user != null) {
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("user", user);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error during login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Checks if an email address is already registered.
     * 
     * Query parameter:
     * - email: Email address to check
     * 
     * Useful for real-time validation during registration.
     * 
     * Response (200 OK):
     * - exists: true if email is registered, false otherwise
     * 
     * Response (500): Empty response on error
     * 
     * @param email Email address to check
     * @return ResponseEntity with existence status
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        Map<String, Boolean> response = new HashMap<>();
        try {
            boolean exists = userService.emailExists(email);
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates an existing user's information.
     * 
     * Path parameter:
     * - userId: ID of user to update
     * 
     * Request body: AppUser object with updated fields
     * 
     * Validation:
     * - User must exist
     * - If changing email, new email must be unique
     * 
     * The user ID is preserved (cannot be changed).
     * 
     * Response (200 OK):
     * - success: true
     * - message: "User updated successfully"
     * - user: Updated AppUser object
     * 
     * Response (404 Not Found):
     * - success: false
     * - message: "User not found"
     * 
     * Response (409 Conflict):
     * - success: false
     * - message: "Email already exists"
     * 
     * Response (500):
     * - success: false
     * - message: Error description
     * 
     * @param userId      ID of user to update
     * @param updatedUser AppUser object with new data
     * @return ResponseEntity with updated user or error
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable int userId, @RequestBody AppUser updatedUser) {
        Map<String, Object> response = new HashMap<>();

        try {
            AppUser existingUser = userService.getUserById(userId);
            if (existingUser == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Keep the same ID
            updatedUser.setId(userId);

            // If email is being changed, check if it already exists
            if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
                if (userService.emailExists(updatedUser.getEmail())) {
                    response.put("success", false);
                    response.put("message", "Email already exists");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                }
            }

            boolean success = userService.updateUser(updatedUser);
            if (success) {
                response.put("success", true);
                response.put("message", "User updated successfully");
                response.put("user", updatedUser);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to update user");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Deletes a user account with cascade deletion.
     * 
     * Path parameter:
     * - userId: ID of user to delete
     * 
     * Deletion process (in order):
     * 1. Retrieves all event IDs created by this user
     * 2. Deletes recurrence rules for those events
     * 3. Deletes all events created by this user
     * 4. Deletes the user account
     * 
     * This ensures complete data cleanup and prevents orphaned records.
     * 
     * Response (200 OK):
     * - success: true
     * - message: "User and all associated events and recurrent rules deleted
     * successfully"
     * 
     * Response (404 Not Found):
     * - success: false
     * - message: "User not found"
     * 
     * Response (500):
     * - success: false
     * - message: Error description
     * 
     * @param userId ID of user to delete
     * @return ResponseEntity with success status or error
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable int userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // First, get all event IDs for this user
            List<Integer> eventIds = eventService.getEventIdsByUserId(userId);

            // Delete recurrent rules for these events
            if (!eventIds.isEmpty()) {
                recurrentService.deleteRecurrentRulesByEventIds(eventIds);
            }

            // Then, delete all events created by this user
            eventService.deleteEventsByUserId(userId);

            // Finally, delete the user
            boolean success = userService.deleteUser(userId);
            if (success) {
                response.put("success", true);
                response.put("message", "User and all associated events and recurrent rules deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
