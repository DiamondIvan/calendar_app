package com.example.frontend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class HolidayData {
    public static Map<LocalDate, String> getHolidays(int year) {
        return Collections.emptyMap();
    }

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

    private static LocalDateTime parse(String date) {
        return LocalDateTime.parse(date + "T00:00:00");
    }
}
