package com.smartcampus.crowd.service;

import com.smartcampus.crowd.model.CrowdData;
import com.smartcampus.crowd.repository.CrowdRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * definitive Simulation Engine: Time-based base + controlled variation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CrowdSimulationService {

    private final CrowdRepository crowdRepository;
    private final SystemModeService modeService;

    private static final Random RANDOM = new Random();
    private static final int WINDOW_SIZE = 3;
    private static final int TREND_THRESHOLD = 1;

    @Getter
    private final Map<String, Integer> currentCrowdMap = new ConcurrentHashMap<>();

    private final Map<String, LinkedList<Integer>> rollingWindows = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> aggregationSum = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> aggregationCount = new ConcurrentHashMap<>();

    // ── Tier 1: 15-second simulation (Smooth Controlled Variation) ──────────

    @Scheduled(fixedRate = 15_000)
    public void simulateCrowd() {
        if (!"SIMULATED".equals(modeService.getCurrentMode())) return;
        
        int hour = LocalDateTime.now().getHour();
        List.of("Library", "Canteen", "Fees Office").forEach(loc -> generateForLocation(loc, hour));
    }

    private void generateForLocation(String location, int hour) {
        int base = getBaseValue(location, hour);
        int previous = currentCrowdMap.getOrDefault(location, base);
        
        // 1. Controlled Variation: previous + small change (-3 to +3)
        int change = RANDOM.nextInt(7) - 3;
        int variationValue = previous + change;

        // 2. Combine with base influence (gravitate slightly towards base to prevent drifting too far)
        // newValue = (0.7 * variation) + (0.3 * base)
        int nextValue = (int) Math.round((variationValue * 0.7) + (base * 0.3));

        // 3. Apply Constraints
        int finalValue = nextValue;
        if ("Fees Office".equals(location)) {
            finalValue = Math.max(5, Math.min(40, nextValue)); // Max 40
        } else if ("Canteen".equals(location)) {
            finalValue = Math.max(20, Math.min(120, nextValue)); // Min 20
        } else {
            finalValue = Math.max(5, Math.min(120, nextValue));
        }

        currentCrowdMap.put(location, finalValue);

        // 4. Update Trend Window
        rollingWindows.computeIfAbsent(location, k -> new LinkedList<>());
        LinkedList<Integer> window = rollingWindows.get(location);
        synchronized (window) {
            window.add(finalValue);
            if (window.size() > WINDOW_SIZE) window.removeFirst();
        }

        // 5. Update Hourly Aggregation
        aggregationSum.computeIfAbsent(location, k -> new AtomicInteger(0)).addAndGet(finalValue);
        aggregationCount.computeIfAbsent(location, k -> new AtomicInteger(0)).incrementAndGet();

        log.debug("[Sim] {} -> {} (base={}, hour={})", location, finalValue, base, hour);
    }

    private int getBaseValue(String location, int hour) {
        switch (location) {
            case "Library":
                if (hour >= 9 && hour <= 11) return 70; // Morning High
                if (hour >= 12 && hour <= 14) return 50; // Afternoon Medium
                if (hour >= 15 && hour <= 17) return 85; // Evening High
                return 15;
            case "Canteen":
                if (hour >= 12 && hour <= 14) return 95; // Lunch Peak
                if (hour >= 15 && hour <= 17) return 50; // Evening Moderate
                if (hour >= 9 && hour <= 11)  return 30; // Morning Low
                return 20;
            case "Fees Office":
                if (hour >= 9 && hour <= 17) return 30; // Steady Day
                return 10;
            default:
                return 25;
        }
    }

    // ── Trend Logic (3-Point In-Memory) ──────────────────────────────────────

    public String getLiveTrend(String location) {
        LinkedList<Integer> window = rollingWindows.get(location);
        if (window == null || window.size() < 3) return "Stable";

        List<Integer> snapshot;
        synchronized (window) { snapshot = new ArrayList<>(window); }

        int c1 = snapshot.get(0); // oldest
        int c3 = snapshot.get(2); // latest
        int delta = c3 - c1;

        if (delta > TREND_THRESHOLD)  return "Increasing";
        if (delta < -TREND_THRESHOLD) return "Decreasing";
        return "Stable";
    }

    // ── Prediction Logic (Trend-based) ───────────────────────────────────────

    public int getLivePrediction(String location) {
        LinkedList<Integer> window = rollingWindows.get(location);
        if (window == null || window.size() < 2) return currentCrowdMap.getOrDefault(location, 20);

        List<Integer> snapshot;
        synchronized (window) { snapshot = new ArrayList<>(window); }
        
        int current = snapshot.get(snapshot.size() - 1);
        int previous = snapshot.get(snapshot.size() - 2);
        
        int trend = current - previous;
        int predicted = current + trend;
        
        return Math.max(0, Math.min(120, predicted));
    }

    // ── Hourly Aggregation (DB Write) ────────────────────────────────────────

    @Scheduled(fixedRate = 3_600_000, initialDelay = 3_600_000)
    public void aggregateToDB() {
        if (!"SIMULATED".equals(modeService.getCurrentMode())) return;
        LocalDateTime now = LocalDateTime.now();
        aggregationSum.keySet().forEach(location -> {
            int sum = aggregationSum.get(location).getAndSet(0);
            int count = aggregationCount.get(location).getAndSet(0);
            if (count > 0) {
                crowdRepository.save(CrowdData.builder()
                        .location(location).crowdCount(Math.round((float) sum / count))
                        .status(calculateStatus(sum/count)).source("Simulated")
                        .timestamp(now).build());
            }
        });
    }

    private String calculateStatus(int count) {
        if (count < 40) return "LOW";
        if (count <= 80) return "MEDIUM";
        return "HIGH";
    }
}
