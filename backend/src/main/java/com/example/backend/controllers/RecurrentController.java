package com.example.backend.controllers;

import com.example.backend.model.Event;
import com.example.backend.service.RecurrentCsvService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recurrent")
@CrossOrigin(origins = "*")
public class RecurrentController {

    private final RecurrentCsvService recurrentService;

    public RecurrentController() {
        this.recurrentService = new RecurrentCsvService();
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllRecurrentRules() {
        try {
            List<Event> rules = recurrentService.loadRecurrentRules();
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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
