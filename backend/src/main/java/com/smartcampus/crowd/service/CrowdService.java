package com.smartcampus.crowd.service;

import com.smartcampus.crowd.dto.CrowdRequest;
import com.smartcampus.crowd.dto.CrowdResponse;
import com.smartcampus.crowd.model.CrowdData;
import com.smartcampus.crowd.repository.CrowdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrowdService {

    private final CrowdRepository crowdRepository;
    private final SystemModeService modeService;
    private final CrowdSimulationService simulationService;

    public CrowdData updateCrowdData(CrowdRequest request) {
        CrowdData crowdData = CrowdData.builder()
                .location(request.getLocation())
                .crowdCount(request.getCrowdCount())
                .source(request.getSource())
                .status(calculateStatus(request.getCrowdCount()))
                .timestamp(LocalDateTime.now())
                .build();
        return crowdRepository.save(crowdData);
    }

    public CrowdResponse getCurrentData(String location) {
        String mode = modeService.getCurrentMode();
        if ("LIVE".equals(mode)) {
            return CrowdResponse.builder()
                    .location(location)
                    .count(0)
                    .status("NO DATA")
                    .trend("N/A")
                    .bestTime(getBestTime(location))  // always from Historical data
                    .prediction(0)
                    .waitingTime(0)
                    .lastUpdated(LocalDateTime.now())
                    .source("Live")
                    .build();
        }

        if ("SIMULATED".equals(mode)) {
            log.info("Using simulated data for current [location={}]", location);

            // ── Fast path: read from in-memory map (no DB query) ──────────────
            Integer inMemoryCrowd = simulationService.getCurrentCrowdMap().get(location);
            if (inMemoryCrowd != null) {
                log.info("[InMemory] {} → crowd={}", location, inMemoryCrowd);
                return CrowdResponse.builder()
                        .location(location)
                        .count(inMemoryCrowd)
                        .status(calculateStatus(inMemoryCrowd))
                        .trend(getTrend(location))
                        .bestTime(getBestTime(location))
                        .prediction(getPrediction(location))
                        .waitingTime(calculateWaitingTime(location, inMemoryCrowd))
                        .lastUpdated(LocalDateTime.now())
                        .source("Simulated")
                        .build();
            }

            // ── Fallback: simulation hasn't ticked yet — try last DB row ──────
            Optional<CrowdData> latest = crowdRepository
                    .findTopByLocationAndSourceOrderByTimestampDesc(location, "Simulated");
            if (latest.isPresent()) {
                return buildResponse(latest.get());
            }

            // ── Warm-start: use historical average so dashboard is never 0 ────
            int currentHour = LocalDateTime.now().getHour();
            Double histAvg = crowdRepository.getAverageCrowdByLocationAndHour(
                    location, currentHour, "Historical");
            if (histAvg == null) {
                histAvg = crowdRepository.getPredictionAvgAnyDay(location, currentHour);
            }
            int warmCount = histAvg != null ? (int) Math.round(histAvg) : 20;
            log.info("[WarmStart] {} hour={} histAvg={} warmCount={}", location, currentHour, histAvg, warmCount);

            return CrowdResponse.builder()
                    .location(location)
                    .count(warmCount)
                    .status(calculateStatus(warmCount))
                    .trend(getTrend(location))
                    .bestTime(getBestTime(location))
                    .prediction(getPrediction(location))
                    .waitingTime(calculateWaitingTime(location, warmCount))
                    .lastUpdated(LocalDateTime.now())
                    .source("Simulated")
                    .build();
        }

        return CrowdResponse.builder()
                .location(location).count(0).source("None").build();
    }

    public List<CrowdData> getHistory(String location) {
        List<CrowdData> history = crowdRepository
                .findTop10ByLocationAndSourceOrderByTimestampDesc(location, "Historical");
        List<CrowdData> mutableHistory = new ArrayList<>(history);
        Collections.reverse(mutableHistory);
        return mutableHistory;
    }

    /**
     * Trend: deterministic comparison of last 3 DB records.
     * Returns "Stable" for ambiguous/flat movement — no randomness.
     */
    public String getTrend(String location) {
        List<CrowdData> recent = crowdRepository.findTop10ByLocationOrderByTimestampDesc(location);
        if (recent.size() < 3) return "Increasing";

        int c1 = recent.get(0).getCrowdCount(); // newest
        int c2 = recent.get(1).getCrowdCount();
        int c3 = recent.get(2).getCrowdCount();

        if (c1 > c2 && c2 > c3) return "Increasing";
        if (c1 < c2 && c2 < c3) return "Decreasing";
        return "Stable";
    }

    /**
     * Best Time: deterministic — always returns the hour (9–17) with the lowest
     * average crowd for this location on today's day-of-week.
     * Tie-break: earliest hour wins (guaranteed by ORDER BY avg_count ASC, hour ASC).
     */
    public String getBestTime(String location) {
        // Primary: today's day-of-week
        List<Object[]> rows = crowdRepository.getBestHoursForLocation(location);

        // Fallback: any day (always has data once seeder has run)
        if (rows == null || rows.isEmpty()) {
            log.warn("[BestTime] No day-specific data for {}; trying any-day fallback", location);
            rows = crowdRepository.getBestHoursForLocationAnyDay(location);
        }

        if (rows == null || rows.isEmpty()) {
            log.warn("[BestTime] Still no data for {} — returning default", location);
            return "9:00 - 10:00";
        }

        int bestHour = ((Number) rows.get(0)[0]).intValue();
        log.info("[BestTime] {} → {}:00", location, bestHour);
        return String.format("%02d:00 - %02d:00", bestHour, (bestHour + 1) % 24);
    }

    /**
     * Prediction: trend-based incremental model.
     *
     * delta  = +3 / -3 / 0  based on current trend direction
     * raw    = currentCrowd + delta
     * smooth = 0.8 * currentCrowd + 0.2 * raw   (keeps result anchored near current)
     * final  = clamp(smooth, 0–120) then clamp within ±10 of currentCrowd
     */
    public int getPrediction(String location) {

        // ── 1. Current crowd — in-memory map first, DB fallback ───────────────
        Integer mapped = simulationService.getCurrentCrowdMap().get(location);
        int currentCrowd;
        if (mapped != null) {
            currentCrowd = mapped;
        } else {
            currentCrowd = crowdRepository
                    .findTopByLocationAndSourceOrderByTimestampDesc(location, "Simulated")
                    .map(CrowdData::getCrowdCount)
                    .orElseGet(() -> {
                        Double h = crowdRepository.getPredictionAvgAnyDay(
                                location, LocalDateTime.now().getHour());
                        return h != null ? (int) Math.round(h) : 20;
                    });
        }

        // ── 2. Trend delta (no historical average used) ───────────────────────
        String trend = getTrend(location);
        int delta;
        if      ("Increasing".equals(trend)) delta =  3;
        else if ("Decreasing".equals(trend)) delta = -3;
        else                                 delta =  0;

        // ── 3. Raw prediction + smoothing ─────────────────────────────────────
        int raw        = currentCrowd + delta;
        int prediction = (int) Math.round(0.8 * currentCrowd + 0.2 * raw);

        // ── 4. Clamp 0–120, then ±10 of current ──────────────────────────────
        prediction = Math.max(0, Math.min(120, prediction));
        prediction = Math.max(currentCrowd - 10, Math.min(currentCrowd + 10, prediction));

        log.info("[Prediction] {} → crowd={} trend={} delta={} final={}", location, currentCrowd, trend, delta, prediction);
        return prediction;
    }

    public CrowdResponse getHistoricalAnalytics(String location) {
        int actualHour = LocalDateTime.now().getHour();
        Double historicalAvg = crowdRepository.getAverageCrowdByLocationAndHour(
                location, actualHour, "Historical");
        int count = historicalAvg != null ? historicalAvg.intValue() : 20;

        return CrowdResponse.builder()
                .location(location)
                .count(count)
                .status(calculateStatus(count))
                .trend(getTrend(location))
                .bestTime(getBestTime(location))
                .prediction(getPrediction(location))
                .waitingTime(calculateWaitingTime(location, count))
                .source("Historical")
                .build();
    }

    private String calculateStatus(int count) {
        if (count < 40) return "LOW";
        if (count <= 80) return "MEDIUM";
        return "HIGH";
    }

    /**
     * Deterministic waiting-time model based on location type.
     *
     * Library / Canteen  — seat-based model:
     *   capacity = 60 seats, service rate = 5 people/min
     *   waitingTime = max(0, crowd - 60) / 5
     *   Examples: crowd=40 → 0 min | crowd=80 → 4 min | crowd=100 → 8 min
     *
     * Fees Office — single-server queue model:
     *   serviceTimePerPerson = 2 minutes
     *   waitingTime = crowd * 2
     *   Examples: crowd=5 → 10 min | crowd=7 → 14 min | crowd=10 → 20 min
     */
    private int calculateWaitingTime(String location, int crowd) {
        int waitingTime;

        if ("Library".equals(location) || "Canteen".equals(location)) {
            int capacity = 60;
            int waitingPeople = Math.max(0, crowd - capacity);
            waitingTime = waitingPeople / 5;

        } else if ("Fees Office".equals(location)) {
            // Queue-based: every person in the queue adds 2 minutes
            waitingTime = crowd * 2;

        } else {
            waitingTime = 0;
        }

        // Safety clamp — waiting time can never be negative
        waitingTime = Math.max(0, waitingTime);

        log.info("[WaitingTime] location={} crowd={} waitingTime={}min", location, crowd, waitingTime);
        return waitingTime;
    }

    private CrowdResponse buildResponse(CrowdData data) {
        return CrowdResponse.builder()
                .location(data.getLocation())
                .count(data.getCrowdCount())
                .status(data.getStatus())
                .trend(getTrend(data.getLocation()))
                .bestTime(getBestTime(data.getLocation()))
                .prediction(getPrediction(data.getLocation()))
                .waitingTime(calculateWaitingTime(data.getLocation(), data.getCrowdCount()))
                .lastUpdated(data.getTimestamp())
                .source(data.getSource())
                .build();
    }

    public List<Map<String, Object>> getHistoricalHourlyTrend(String location) {
        List<Object[]> rawData = crowdRepository.getHistoricalHourlyTrend(location);

        // Build a map of hour → avg from DB results
        Map<Integer, Integer> hourMap = new HashMap<>();
        for (Object[] row : rawData) {
            int hour = ((Number) row[0]).intValue();
            int avg  = (int) Math.round(((Number) row[1]).doubleValue());
            if (hour >= 8 && hour <= 19) {
                hourMap.put(hour, avg);
            }
        }

        log.info("[Trend] {} → DB returned {} hour buckets: {}", location, hourMap.size(), hourMap);

        // Build full 8–19 list; carry forward last known avg for any gap
        List<Map<String, Object>> result = new ArrayList<>();
        int lastAvg = 0;
        for (int h = 8; h <= 19; h++) {
            int avg = hourMap.getOrDefault(h, lastAvg);
            if (hourMap.containsKey(h)) lastAvg = avg;
            Map<String, Object> entry = new HashMap<>();
            entry.put("hour", h);
            entry.put("avg", avg);
            result.add(entry);
        }

        return result;
    }
}
