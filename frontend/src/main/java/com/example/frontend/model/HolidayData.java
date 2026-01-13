package com.example.frontend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * HolidayData provides Malaysian public holiday information.
 * 
 * Currently contains:
 * - A stub method for getting holidays by year (returns empty map)
 * - Hard-coded 2026 Malaysian public holidays as Event objects
 * 
 * The 2026 holidays include major Malaysian holidays such as:
 * New Year, Chinese New Year, Hari Raya, Deepavali, Christmas, etc.
 * Regional holidays for specific states (Sabah, Sarawak, etc.) are also
 * included.
 */
public class HolidayData {
    /**
     * Gets holidays for a specific year.
     * 
     * Currently a stub implementation that returns an empty map.
     * Future implementation could return a map of dates to holiday names.
     * 
     * @param year The year to get holidays for
     * @return Map of LocalDate to holiday name (currently empty)
     */
    public static Map<LocalDate, String> getHolidays(int year) {
        return Collections.emptyMap();
    }

    /**
     * Returns a list of Malaysian public holidays for 2026 as Event objects.
     * 
     * Each holiday is created as an Event with:
     * - Negative ID (-1000 to -1023) to avoid collisions with user events
     * - User ID of -1 (system/public event)
     * - Holiday name as title
     * - "Public Holiday" as description
     * - Date set to midnight (00:00:00) of the holiday
     * - Category set to "HOLIDAY"
     * 
     * Includes 24 major Malaysian holidays covering all states and territories.
     * 
     * @return List of Event objects representing 2026 Malaysian public holidays
     */
    public static List<Event> getHolidays2026() {
        List<Event> holidays = new ArrayList<>();

        String[] dates = {
                "2026-01-01", "2026-01-16", "2026-02-01", "2026-02-14", "2026-02-17", "2026-02-19",
                "2026-03-07", "2026-03-20", "2026-04-03", "2026-04-05", "2026-05-01", "2026-05-26",
                "2026-05-27", "2026-05-30", "2026-05-31", "2026-06-01", "2026-06-17", "2026-08-25",
                "2026-08-31", "2026-09-16", "2026-11-08", "2026-12-24", "2026-12-25", "2026-12-31"
        };

        String[] names = {
                "New Year's Day", "Isra and Mi'raj", "Federal Territory Day and Thaipusam", "Valentine's Day",
                "Chinese New Year's Day", "First Day of Ramadan", "Nuzul Al-Quran", "Hari Raya Puasa",
                "Good Friday (Sabah, Sarawak)", "Easter Sunday", "Labour Day",
                "Day of Arafat (Kelantan, Terengganu)", "Hari Raya Haji", "Harvest Festival (Labuan, Sabah)",
                "Wesak Day and Second Day of Harvest Festival (Labuan, Sabah)",
                "The Yang di-Pertuan Agong's Birthday and Second Day of Harvest Festival observed (Labuan, Sabah)",
                "Muharram", "The Prophet Muhammad's Birthday", "Malaysia's National Day", "Malaysia Day",
                "Diwali (Most regions)", "Christmas Eve", "Christmas Day", "New Year's Eve"
        };

        for (int i = 0; i < dates.length && i < names.length; i++) {
            Event e = new Event();
            e.setId(-1000 - i); // Unique negative ID to avoid collisions with real events
            e.setUserId(-1); // System user
            e.setTitle(names[i]);
            e.setDescription("Public Holiday");
            e.setStartDateTime(parse(dates[i]));
            e.setEndDateTime(parse(dates[i]));
            e.setCategory("HOLIDAY");
            holidays.add(e);
        }

        return holidays;
    }

    /**
     * Parses a date string in "YYYY-MM-DD" format to LocalDateTime at midnight.
     * 
     * @param date Date string in format "YYYY-MM-DD" (e.g., "2026-01-01")
     * @return LocalDateTime representing the date at 00:00:00
     */
    private static LocalDateTime parse(String date) {
        return LocalDateTime.parse(date + "T00:00:00");
    }
}
