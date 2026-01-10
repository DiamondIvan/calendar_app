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

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventCsvService eventService;

    public EventController() {
        this.eventService = new EventCsvService();
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        try {
            List<Event> events = eventService.loadEvents();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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
