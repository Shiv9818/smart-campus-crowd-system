package com.smartcampus.crowd.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-location hourly crowd ranges for the live simulation engine.
 *
 * Covers all 24 hours (0–23) for 3 locations, matching DataSeeder's
 * range-based logic exactly so simulation and historical data tell
 * a consistent story.
 *
 *   Library:     steady 9–21, peak 12–14
 *   Canteen:     lunch peak 12–15, dinner surge 18–21
 *   Fees Office: active 9–17 only, near-empty otherwise
 */
public class SimulationRangeModel {

    public record Range(int min, int max) {}

    /** Locations the simulation engine will tick every 15 s. */
    public static final Map<String, Map<Integer, Range>> RANGES;

    static {
        RANGES = new LinkedHashMap<>();

        RANGES.put("Library",     buildLibrary());
        RANGES.put("Canteen",     buildCanteen());
        RANGES.put("Fees Office", buildFeesOffice());
    }

    // ── Library ───────────────────────────────────────────────────────────────
    private static Map<Integer, Range> buildLibrary() {
        Map<Integer, Range> m = new LinkedHashMap<>();
        //  0– 5 → 5–10
        for (int h =  0; h <=  5; h++) m.put(h, new Range(  5,  10));
        //  6– 8 → 20–40
        for (int h =  6; h <=  8; h++) m.put(h, new Range( 20,  40));
        //  9–11 → 60–100
        for (int h =  9; h <= 11; h++) m.put(h, new Range( 60, 100));
        // 12–13 → 80–120  (PEAK)
        for (int h = 12; h <= 13; h++) m.put(h, new Range( 80, 120));
        // 14–16 → 60–90
        for (int h = 14; h <= 16; h++) m.put(h, new Range( 60,  90));
        // 17–20 → 30–60
        for (int h = 17; h <= 20; h++) m.put(h, new Range( 30,  60));
        // 21–23 → 20–40
        for (int h = 21; h <= 23; h++) m.put(h, new Range( 20,  40));
        return m;
    }

    // ── Canteen ───────────────────────────────────────────────────────────────
    private static Map<Integer, Range> buildCanteen() {
        Map<Integer, Range> m = new LinkedHashMap<>();
        for (int h =  0; h <=  5; h++) m.put(h, new Range(  5,  10));
        for (int h =  6; h <=  8; h++) m.put(h, new Range( 20,  40));
        for (int h =  9; h <= 11; h++) m.put(h, new Range( 40,  70));
        // 12–14 → lunch PEAK
        for (int h = 12; h <= 14; h++) m.put(h, new Range(100, 120));
        for (int h = 15; h <= 17; h++) m.put(h, new Range( 50,  80));
        // 18–20 → dinner surge
        for (int h = 18; h <= 20; h++) m.put(h, new Range( 80, 110));
        for (int h = 21; h <= 23; h++) m.put(h, new Range( 20,  40));
        return m;
    }

    // ── Fees Office ───────────────────────────────────────────────────────────
    private static Map<Integer, Range> buildFeesOffice() {
        Map<Integer, Range> m = new LinkedHashMap<>();
        for (int h =  0; h <=  5; h++) m.put(h, new Range(  0,   5));
        for (int h =  6; h <=  8; h++) m.put(h, new Range( 10,  20));
        // 9–16 → active office hours
        for (int h =  9; h <= 16; h++) m.put(h, new Range( 20,  70));
        for (int h = 17; h <= 23; h++) m.put(h, new Range(  5,  20));
        return m;
    }

    /**
     * Returns the range for a given location and hour (0–23).
     * Falls back to a near-zero range for any unregistered combination.
     */
    public static Range getRangeForHour(String location, int hour) {
        Map<Integer, Range> loc = RANGES.get(location);
        if (loc != null) {
            Range r = loc.get(hour);
            if (r != null) return r;
        }
        return new Range(0, 5);
    }
}
