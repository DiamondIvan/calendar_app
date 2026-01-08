package com.example.backend.utils;

import com.example.backend.model.Event;
import java.util.ArrayList;
import java.util.List;

public class StaticEvents {

    public static final List<Event> STATIC_SUGGESTIONS = new ArrayList<>();

    static {
        // Initialize with default events if needed
        // Event format: id, userId, title, description, start, end, category
        STATIC_SUGGESTIONS.add(new Event(0, 0, "Meeting", "Weekly sync", "2024-01-01", "2024-01-01", "WORK"));
        STATIC_SUGGESTIONS.add(new Event(0, 0, "Gym", "Workout", "2024-01-01", "2024-01-01", "PERSONAL"));
    }
}