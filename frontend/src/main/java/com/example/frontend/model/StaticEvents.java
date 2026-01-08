package com.example.frontend.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StaticEvents {
    // Should be List<Event> and initialized with some static events if needed
    public static final List<Event> STATIC_SUGGESTIONS = Collections.emptyList();

    public static Map<String, Integer> getCategoryCounts(List<Event> events) {
        return Collections.emptyMap();
    }
}
