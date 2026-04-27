// DataStore.java
package com.demo;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataStore {

    // ── File path ─────────────────────────────────────────────────────────────
    private static final String DATA_DIR  = System.getProperty("user.home") + "/.flowstate";
    private static final String DATA_FILE = DATA_DIR + "/sessions.json";

    // ── Gson with LocalDate / LocalDateTime adapters ──────────────────────────
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(LocalDate.class,     new LocalDateAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    // ── In-memory state ───────────────────────────────────────────────────────
    private AppData data;

    // Singleton
    private static DataStore instance;
    public static DataStore get() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    private DataStore() {
        data = load();
    }

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /** Call this after every completed WORK session */
    public void recordSession(String category, int durationMinutes, int focusRating) {
        SessionRecord rec = new SessionRecord();
        rec.date          = LocalDateTime.now();
        rec.category      = category;
        rec.durationMinutes = durationMinutes;
        rec.focusRating   = focusRating;   // 1–5, 0 if not rated
        data.sessions.add(rec);
        save();
    }

    /** Save a focus rating for the most recent session */
    public void rateLastSession(int rating) {
        if (!data.sessions.isEmpty()) {
            data.sessions.get(data.sessions.size() - 1).focusRating = rating;
            save();
        }
    }

    // ── Daily stats ───────────────────────────────────────────────────────────

    public int getTodaySessionCount() {
        LocalDate today = LocalDate.now();
        return (int) data.sessions.stream()
            .filter(s -> s.date.toLocalDate().equals(today))
            .count();
    }

    public int getTodayFocusMinutes() {
        LocalDate today = LocalDate.now();
        return data.sessions.stream()
            .filter(s -> s.date.toLocalDate().equals(today))
            .mapToInt(s -> s.durationMinutes)
            .sum();
    }

    // ── Weekly stats ──────────────────────────────────────────────────────────

    public int getWeekSessionCount() {
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        return (int) data.sessions.stream()
            .filter(s -> s.date.toLocalDate().isAfter(weekAgo))
            .count();
    }

    public int getWeekFocusMinutes() {
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        return data.sessions.stream()
            .filter(s -> s.date.toLocalDate().isAfter(weekAgo))
            .mapToInt(s -> s.durationMinutes)
            .sum();
    }

    /** Best day this week — returns day name e.g. "Monday" */
    public String getBestDayThisWeek() {
        Map<LocalDate, Integer> byDay = new HashMap<>();
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        data.sessions.stream()
            .filter(s -> s.date.toLocalDate().isAfter(weekAgo))
            .forEach(s -> byDay.merge(s.date.toLocalDate(), s.durationMinutes, Integer::sum));
        return byDay.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(e -> e.getKey().getDayOfWeek().toString().charAt(0)
                    + e.getKey().getDayOfWeek().toString().substring(1).toLowerCase())
            .orElse("—");
    }

    // ── Category breakdown ────────────────────────────────────────────────────

    /** Returns map of category → total minutes (all time) */
    public Map<String, Integer> getCategoryMinutes() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (SessionRecord s : data.sessions) {
            map.merge(s.category, s.durationMinutes, Integer::sum);
        }
        return map;
    }

    /** Top category by total minutes */
    public String getTopCategory() {
        return getCategoryMinutes().entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("—");
    }

    // ── Streak ────────────────────────────────────────────────────────────────

    /** Consecutive days (ending today) with at least one session */
    public int getCurrentStreak() {
        Set<LocalDate> activeDays = new HashSet<>();
        data.sessions.forEach(s -> activeDays.add(s.date.toLocalDate()));

        int streak = 0;
        LocalDate day = LocalDate.now();
        while (activeDays.contains(day)) {
            streak++;
            day = day.minusDays(1);
        }
        return streak;
    }

    // ── Heatmap data ──────────────────────────────────────────────────────────

    /** Returns date → session count for the past N days */
    public Map<LocalDate, Integer> getHeatmapData(int days) {
        Map<LocalDate, Integer> map = new HashMap<>();
        LocalDate cutoff = LocalDate.now().minusDays(days);
        data.sessions.stream()
            .filter(s -> s.date.toLocalDate().isAfter(cutoff))
            .forEach(s -> map.merge(s.date.toLocalDate(), 1, Integer::sum));
        return map;
    }

    // ── Focus rating by hour (for FocusDNA bar chart) ─────────────────────────

    /** Returns hour (0–23) → average focus rating */
    public Map<Integer, Double> getAvgRatingByHour() {
        Map<Integer, List<Integer>> byHour = new HashMap<>();
        for (SessionRecord s : data.sessions) {
            if (s.focusRating > 0) {
                byHour.computeIfAbsent(s.date.getHour(), k -> new ArrayList<>()).add(s.focusRating);
            }
        }
        Map<Integer, Double> result = new TreeMap<>();
        byHour.forEach((hour, ratings) ->
            result.put(hour, ratings.stream().mapToInt(i -> i).average().orElse(0)));
        return result;
    }

    // ── Badge helpers ─────────────────────────────────────────────────────────

    /** True if any session started before 9am */
    public boolean hasEarlyBirdSession() {
        return data.sessions.stream().anyMatch(s -> s.date.getHour() < 9);
    }

    /** True if streak >= 3 days */
    public boolean isOnFire() {
        return getCurrentStreak() >= 3;
    }

    /** True if avg rating across all sessions >= 4.0 */
    public boolean isZenMaster() {
        OptionalDouble avg = data.sessions.stream()
            .filter(s -> s.focusRating > 0)
            .mapToInt(s -> s.focusRating)
            .average();
        return avg.isPresent() && avg.getAsDouble() >= 4.0;
    }

    /** True if any single day has >= 4 sessions */
    public boolean isNoMercy() {
        Map<LocalDate, Long> byDay = new HashMap<>();
        data.sessions.forEach(s -> byDay.merge(s.date.toLocalDate(), 1L, Long::sum));
        return byDay.values().stream().anyMatch(c -> c >= 4);
    }

    /** All sessions list (read-only copy) */
    public List<SessionRecord> getAllSessions() {
        return Collections.unmodifiableList(data.sessions);
    }

    // =========================================================================
    // PERSISTENCE
    // =========================================================================

    private void save() {
        try {
            new File(DATA_DIR).mkdirs();
            try (Writer w = new FileWriter(DATA_FILE)) {
                GSON.toJson(data, w);
            }
        } catch (IOException e) {
            System.err.println("DataStore save error: " + e.getMessage());
        }
    }

    private AppData load() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return new AppData();
        try (Reader r = new FileReader(f)) {
            Type type = new TypeToken<AppData>(){}.getType();
            AppData loaded = GSON.fromJson(r, type);
            return loaded != null ? loaded : new AppData();
        } catch (IOException e) {
            System.err.println("DataStore load error: " + e.getMessage());
            return new AppData();
        }
    }

    // =========================================================================
    // DATA MODELS
    // =========================================================================

    private static class AppData {
        List<SessionRecord> sessions = new ArrayList<>();
    }

    public static class SessionRecord {
        public LocalDateTime date;
        public String        category;
        public int           durationMinutes;
        public int           focusRating;   // 0 = not yet rated
    }

    // =========================================================================
    // GSON TYPE ADAPTERS
    // =========================================================================

    private static class LocalDateAdapter
            implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE;
        @Override public JsonElement serialize(LocalDate d, java.lang.reflect.Type t, JsonSerializationContext c) {
            return new JsonPrimitive(d.format(FMT));
        }
        @Override public LocalDate deserialize(JsonElement j, java.lang.reflect.Type t, JsonDeserializationContext c) {
            return LocalDate.parse(j.getAsString(), FMT);
        }
    }

    private static class LocalDateTimeAdapter
            implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        @Override public JsonElement serialize(LocalDateTime d, java.lang.reflect.Type t, JsonSerializationContext c) {
            return new JsonPrimitive(d.format(FMT));
        }
        @Override public LocalDateTime deserialize(JsonElement j, java.lang.reflect.Type t, JsonDeserializationContext c) {
            return LocalDateTime.parse(j.getAsString(), FMT);
        }
    }
}