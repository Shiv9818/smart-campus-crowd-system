package com.smartcampus.crowd.config;

import com.smartcampus.crowd.model.CrowdData;
import com.smartcampus.crowd.repository.CrowdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Seeds 60 days × 3 locations × 24 hours = 4,320 Historical records.
 *
 * Location patterns:
 *   Library:    steady 9–21, peak 12–14
 *   Canteen:    lunch peak 12–15, dinner surge 18–21
 *   Fees Office:busy 9–17 only, near-empty all other hours
 *
 * Applied per-record: ±10% noise, ±1 daily hour shift, Mon/Fri boost, clamp 0–120.
 * Fees Office gets an additional 30% spike chance.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CrowdRepository crowdRepository;
    private static final Random RANDOM = new Random(13);

    private static final String[] LOCATIONS = { "Library", "Canteen", "Fees Office" };

    /** Returns [min, max] for a location at a given hour (0-23). */
    private static int[] range(String location, int hour) {
        int h = Math.max(0, Math.min(23, hour));
        return switch (location) {
            case "Library" -> {
                if (h <=  5) yield new int[]{  5,  10 };
                if (h <=  8) yield new int[]{ 20,  40 };
                if (h <= 11) yield new int[]{ 60, 100 };
                if (h <= 13) yield new int[]{ 80, 120 };
                if (h <= 16) yield new int[]{ 60,  90 };
                if (h <= 20) yield new int[]{ 30,  60 };
                             yield new int[]{ 20,  40 };
            }
            case "Canteen" -> {
                if (h <=  5) yield new int[]{  5,  10 };
                if (h <=  8) yield new int[]{ 20,  40 };
                if (h <= 11) yield new int[]{ 40,  70 };
                if (h <= 14) yield new int[]{100, 120 };
                if (h <= 17) yield new int[]{ 50,  80 };
                if (h <= 20) yield new int[]{ 80, 110 };
                             yield new int[]{ 20,  40 };
            }
            case "Fees Office" -> {
                if (h <=  5) yield new int[]{  0,   5 };
                if (h <=  8) yield new int[]{ 10,  20 };
                if (h <= 16) yield new int[]{ 20,  70 };
                             yield new int[]{  5,  20 };
            }
            default -> new int[]{ 0, 0 };
        };
    }

    @Override
    public void run(String... args) {
        log.info("=== [DataSeeder] Clearing ALL data — regenerating 60-day dataset (3 locations × 24 h) ===");
        crowdRepository.deleteAll();
        seedData();
        log.info("=== [DataSeeder] Complete — {} total records ===", crowdRepository.count());
    }

    private void seedData() {
        LocalDateTime now = LocalDateTime.now();
        List<CrowdData> batch = new ArrayList<>();

        for (int day = 60; day >= 1; day--) {
            LocalDateTime dayBase = now.minusDays(day);
            DayOfWeek dow = dayBase.getDayOfWeek();
            boolean busyDay = (dow == DayOfWeek.MONDAY || dow == DayOfWeek.FRIDAY);

            // ±15% daily multiplier
            double dailyMult = 0.85 + RANDOM.nextDouble() * 0.30;

            // ±1 hour shift (applies to range lookup only)
            int shift = RANDOM.nextInt(3) - 1;

            for (String location : LOCATIONS) {
                for (int hour = 0; hour <= 23; hour++) {

                    int[] r    = range(location, hour + shift);
                    int   min  = r[0], max = r[1];

                    // Random base within [min, max]
                    int base  = (max > min) ? min + RANDOM.nextInt(max - min + 1) : min;

                    // Daily multiplier + ±10% per-record noise
                    double noise = 0.90 + RANDOM.nextDouble() * 0.20;
                    int count = (int) Math.max(0, Math.round(base * dailyMult * noise));

                    // Monday / Friday crowd boost: +10 to +20
                    if (busyDay) {
                        count += 10 + RANDOM.nextInt(11);
                    }

                    // Fees Office: 30% spike +20 to +40
                    if ("Fees Office".equals(location) && RANDOM.nextInt(100) < 30) {
                        count += 20 + RANDOM.nextInt(21);
                    }

                    // Clamp 0–120
                    count = Math.max(0, Math.min(120, count));

                    LocalDateTime ts = dayBase
                            .withHour(hour)
                            .withMinute(0)
                            .withSecond(0)
                            .withNano(0);

                    batch.add(CrowdData.builder()
                            .location(location)
                            .crowdCount(count)
                            .status(status(count))
                            .source("Historical")
                            .timestamp(ts)
                            .build());
                }
            }

            crowdRepository.saveAll(batch);
            batch.clear();

            if (day % 15 == 0 || day <= 2) {
                log.info("  T-{} ({}, mult={}, shift={})", day, dow,
                        String.format("%.2f", dailyMult), shift);
            }
        }
    }

    private String status(int count) {
        if (count < 40)  return "LOW";
        if (count <= 80) return "MEDIUM";
        return "HIGH";
    }
}
