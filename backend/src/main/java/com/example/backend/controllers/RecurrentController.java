package com.example.backend.controllers;

import com.example.backend.model.Event;
import com.example.backend.service.RecurrentCsvService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for recurring event rule management.
 * 
 * Provides HTTP endpoints for:
 * - Managing recurrence rules (CRUD operations)
 * - Linking recurrence patterns to events
 * - Retrieving rules by event ID
 * 
 * Base URL: /api/recurrent
 * 
 * Recurrence rule fields:
 * - eventId: ID of the event this rule applies to
 * - recurrentInterval: "1d", "1w", "1m", "1y"
 * - recurrentTimes: Number of occurrences
 * - recurrentEndDate: End date for recurrence
 * 
 * Uses RecurrentCsvService for data persistence.
 */
@RestController
@RequestMapping("/api/recurrent")
public class RecurrentController {

    /** Service handling recurrent rule operations */
    private final RecurrentCsvService recurrentService;

    /**
     * Constructs a RecurrentController with a new RecurrentCsvService instance.
     */
    public RecurrentController() {
        this.recurrentService = new RecurrentCsvService();
    }

    /**
     * Retrieves all recurrence rules.
     * 
     * Response (200 OK): List of Event objects containing recurrence data
     * Response (500): Empty response on error
     * 
     * @return ResponseEntity containing list of all recurrence rules
     */
    @GetMapping
    public ResponseEntity<List<Event>> getAllRecurrentRules() {
        try {
            List<Event> rules = recurrentService.loadRecurrentRules();
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves recurrence rule for a specific event.
     * 
     * Path parameter:
     * - eventId: ID of the event
     * 
     * Response (200 OK): Event object with recurrence data
     * Response (404 Not Found): No rule exists for this event
     * Response (500): Internal error
     * 
     * @param eventId The event ID
     * @return ResponseEntity containing the recurrence rule or 404
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<Event> getRecurrentRuleByEventId(@PathVariable int eventId) {
        try {
            List<Event> rules = recurrentService.loadRecurrentRules();
            Event rule = rules.stream()
                    .filter(r -> r.getId() == eventId)
                    .findFirst()
                    .orElse(null);

            if (rule != null) {
                return ResponseEntity.ok(rule);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Creates a new recurrence rule.
     * 
     * Request body: Event object with recurrence fields
     * 
     * Validation:
     * - recurrentInterval: Required, non-empty
     * 
     * Response (201 Created):
     * - success: true
     * - message: "Recurrent rule created successfully"
     * - rule: Created Event object
     * 
     * Response (400 Bad Request):
     * - success: false
     * - message: "Recurrent interval is required"
     * 
     * Response (500):
     * - success: false
     * - message: Error description
     * 
     * @param event Event object with recurrence data
     * @return ResponseEntity with created rule or error
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRecurrentRule(@RequestBody Event event) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (event.getRecurrentInterval() == null || event.getRecurrentInterval().isEmpty()) {
                response.put("success", false);
                response.put("message", "Recurrent interval is required");
                return ResponseEntity.badRequest().body(response);
            }

            recurrentService.saveRecurrentRule(event);
            response.put("success", true);
            response.put("message", "Recurrent rule created successfully");
            response.put("rule", event);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating recurrent rule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Updates an existing recurrence rule or creates if not found.
     * 
     * Path parameter:
     * - eventId: ID of the event
     * 
     * Request body: Event object with updated recurrence data
     * 
     * Response (200 OK):
     * - success: true
     * - message: "Recurrent rule updated successfully"
     * - rule: Updated Event object
     * 
     * Response (500):
     * - success: false
     * - message: Error description
     * 
     * @param eventId ID of event whose rule to update
     * @param event   Event object with new recurrence data
     * @return ResponseEntity with updated rule or error
     */
    @PutMapping("/{eventId}")
    public ResponseEntity<Map<String, Object>> updateRecurrentRule(@PathVariable int eventId,
            @RequestBody Event event) {
        Map<String, Object> response = new HashMap<>();

        try {
            recurrentService.updateRecurrentRule(eventId, event);
            response.put("success", true);
            response.put("message", "Recurrent rule updated successfully");
            response.put("rule", event);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating recurrent rule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Deletes a recurrence rule by event ID.
     * 
     * Path parameter:
     * - eventId: ID of the event
     * 
     * Response (200 OK):
     * - success: true
     * - message: "Recurrent rule deleted successfully"
     * 
     * Response (500):
     * - success: false
     * - message: Error description
     * 
     * @param eventId ID of event whose rule to delete
     * @return ResponseEntity with success status or error
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Map<String, Object>> deleteRecurrentRule(@PathVariable int eventId) {
        Map<String, Object> response = new HashMap<>();

        try {
            recurrentService.deleteRecurrentRule(eventId);
            response.put("success", true);
            response.put("message", "Recurrent rule deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting recurrent rule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
