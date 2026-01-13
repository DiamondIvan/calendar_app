package com.example.backend.controllers;

import com.example.backend.model.Event;
import com.example.backend.service.EventCsvService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for event management operations.
 * 
 * Provides HTTP endpoints for:
 * - Creating, reading, updating, and deleting events (CRUD)
 * - Filtering events by user ID or category
 * - Retrieving all events
 * 
 * Base URL: /api/events
 * 
 * All endpoints handle Event objects with fields:
 * - id, userId, title, description
 * - startDateTime, endDateTime, category
 * 
 * Uses EventCsvService for data persistence.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    /** Service handling event data operations */
    private final EventCsvService eventService;

    /**
     * Constructs an EventController with a new EventCsvService instance.
     */
    public EventController() {
        this.eventService = new EventCsvService();
    }

    /**
     * Retrieves all events.
     * 
     * Response (200 OK): List of all Event objects
     * Response (500): Empty response on error
     * 
     * @return ResponseEntity containing list of all events
     */
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        try {
            List<Event> events = eventService.loadEvents();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all events for a specific user.
     * 
     * Path parameter:
     * - userId: ID of the user
     * 
     * Filters events where event.userId matches the provided userId.
     * 
     * Response (200 OK): List of Event objects for this user
     * Response (500): Empty response on error
     * 
     * @param userId The user ID to filter by
     * @return ResponseEntity containing user's events
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Event>> getEventsByUser(@PathVariable int userId) {
        try {
            List<Event> allEvents = eventService.loadEvents();
            List<Event> userEvents = allEvents.stream()
                    .filter(event -> event.getUserId() == userId)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userEvents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a single event by ID.
     * 
     * Path parameter:
     * - id: Event ID
     * 
     * Response (200 OK): Event object
     * Response (404 Not Found): Event doesn't exist
     * Response (500): Internal error
     * 
     * @param id The event ID
     * @return ResponseEntity containing the event or 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable int id) {
        try {
            List<Event> events = eventService.loadEvents();
            Event event = events.stream()
                    .filter(e -> e.getId() == id)
                    .findFirst()
                    .orElse(null);

            if (event != null) {
                return ResponseEntity.ok(event);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Creates a new event.
     * 
     * Request body: Event object (ID auto-generated)
     * 
     * Validation:
     * - title: Required, non-empty
     * - startDateTime: Required
     * 
     * Response (201 Created):
     * - success: true
     * - message: "Event created successfully"
     * - event: Created Event object with assigned ID
     * 
     * Response (400 Bad Request):
     * - success: false
     * - message: Validation error
     * 
     * Response (500):
     * - success: false
     * - message: Error description
     * 
     * @param event Event object to create
     * @return ResponseEntity with created event or error
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEvent(@RequestBody Event event) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (event.getTitle() == null || event.getTitle().isEmpty()) {
                response.put("success", false);
                response.put("message", "Event title is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (event.getStartDateTime() == null) {
                response.put("success", false);
                response.put("message", "Start date/time is required");
                return ResponseEntity.badRequest().body(response);
            }

            eventService.saveEvent(event);
            response.put("success", true);
            response.put("message", "Event created successfully");
            response.put("event", event);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Updates an existing event.
     * 
     * Path parameter:
     * - id: Event ID to update
     * 
     * Request body: Event object with updated fields
     * 
     * Validation:
     * - title: Required, non-empty
     * 
     * Response (200 OK):
     * - success: true
     * - message: "Event updated successfully"
     * - event: Updated Event object
     * 
     * Response (400 Bad Request):
     * - success: false
     * - message: Validation error
     * 
     * Response (500):
     * - success: false
     * - message: Error description
     * 
     * @param id    ID of event to update
     * @param event Event object with new data
     * @return ResponseEntity with updated event or error
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateEvent(@PathVariable int id, @RequestBody Event event) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (event.getTitle() == null || event.getTitle().isEmpty()) {
                response.put("success", false);
                response.put("message", "Event title is required");
                return ResponseEntity.badRequest().body(response);
            }

            eventService.updateEvent(id, event);
            response.put("success", true);
            response.put("message", "Event updated successfully");
            response.put("event", event);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Deletes an event by ID.
     * 
     * Path parameter:
     * - id: Event ID to delete
     * 
     * Response (200 OK):
     * - success: true
     * - message: "Event deleted successfully"
     * 
     * Response (500):
     * - success: false
     * - message: Error description
     * 
     * @param id ID of event to delete
     * @return ResponseEntity with success status or error
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEvent(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();

        try {
            eventService.deleteEvent(id);
            response.put("success", true);
            response.put("message", "Event deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Retrieves all events for a specific category.
     * 
     * Path parameter:
     * - category: Category ID (case-insensitive)
     * 
     * Examples: "PERSONAL", "WORK", "HOLIDAY"
     * 
     * Response (200 OK): List of Event objects in this category
     * Response (500): Empty response on error
     * 
     * @param category The category ID to filter by
     * @return ResponseEntity containing events in this category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Event>> getEventsByCategory(@PathVariable String category) {
        try {
            List<Event> allEvents = eventService.loadEvents();
            List<Event> filteredEvents = allEvents.stream()
                    .filter(event -> category.equalsIgnoreCase(event.getCategory()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(filteredEvents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
