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

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserCsvService userService;
    private final EventCsvService eventService;
    private final RecurrentCsvService recurrentService;

    public UserController() {
        this.userService = new UserCsvService();
        this.eventService = new EventCsvService();
        this.recurrentService = new RecurrentCsvService();
    }

    @GetMapping
    public ResponseEntity<List<AppUser>> getAllUsers() {
        try {
            List<AppUser> users = userService.loadUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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
