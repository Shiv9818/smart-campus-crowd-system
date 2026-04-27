package com.smartcampus.crowd.service;

import com.smartcampus.crowd.model.CrowdData;
import com.smartcampus.crowd.repository.CrowdRepository;
import com.smartcampus.crowd.util.SimulationRangeModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimised simulation engine — two-tier data strategy:
 *
 *  Tier 1 (every 15 s):  Update in-memory currentCrowdMap only.
 *                         NO database write.
 *
 *  Tier 2 (every 10 min): Compute average of buffered values per location.
 *                          Write ONE aggregated row to DB with source="Simulated".
 *                          Clear buffer.
 *
 * This reduces DB writes from ~240/hour (15 s × 3 locations) to just 3/hour.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CrowdSimulationService {

    private final CrowdRepository crowdRepository;
    private final SystemModeService modeService;

    private static final Random RANDOM = new Random();

    /**
     * In-memory store: location → latest crowd count.
     * Read by CrowdService to serve API responses instantly without a DB query.
     */
    @Getter
    private final Map<String, Integer> currentCrowdMap = new ConcurrentHashMap<>();

    /**
     * Rolling buffer: location → list of recent raw counts (up to 40 values per location).
     * Averaged and flushed to DB every 10 minutes by aggregateToDB().
     */
    private final Map<String, List<Integer>> tempStorage = new ConcurrentHashMap<>();

    // ── Tier 1: 15-second in-memory tick ────────────────────────────────────

    @Scheduled(fixedRate = 15_000)
    public void simulateCrowd() {
        if (!"SIMULATED".equals(modeService.getCurrentMode())) return;

        LocalDateTime now = LocalDateTime.now();
        SimulationRangeModel.RANGES.keySet().forEach(location ->
                simulateForLocation(location, now));
    }

    private void simulateForLocation(String location, LocalDateTime now) {
        int hour = now.getHour();

        // Previous in-memory value (or DB fallback on first tick)
        int currentCrowd = currentCrowdMap.computeIfAbsent(location, loc ->
                crowdRepository.findTopByLocationAndSourceOrderByTimestampDesc(loc, "Simulated")
                               .map(CrowdData::getCrowdCount)
                               .orElse(20));

        SimulationRangeModel.Range range = SimulationRangeModel.getRangeForHour(location, hour);
        int center = (range.min() + range.max()) / 2;

        int direction;
        if      (currentCrowd <= range.min()) direction =  1;
        else if (currentCrowd >= range.max()) direction = -1;
        else if (currentCrowd <  center)      direction =  1;
        else if (currentCrowd >  center)      direction = -1;
        else                                  direction = RANDOM.nextBoolean() ? 1 : -1;

        int magnitude   = RANDOM.nextBoolean() ? 1 : 2;
        int finalCrowd  = Math.max(range.min(),
                          Math.min(range.max(), currentCrowd + direction * magnitude));

        // Update in-memory map — NO DB write here
        currentCrowdMap.put(location, finalCrowd);

        // Append to rolling buffer (cap at 40 to bound memory)
        tempStorage.computeIfAbsent(location, k -> Collections.synchronizedList(new ArrayList<>()))
                   .add(finalCrowd);

        List<Integer> buf = tempStorage.get(location);
        if (buf.size() > 40) buf.remove(0);

        log.debug("[Sim] {} → crowd={} (in-memory only, buffer size={})",
                location, finalCrowd, buf.size());
    }

    // ── Tier 2: 10-minute aggregation + DB persist ───────────────────────────

    /**
     * Every 10 minutes: average the buffer, save one row per location to DB.
     * Uses fixedDelay so the 10-min clock starts AFTER the previous write finishes.
     */
    @Scheduled(fixedDelay = 600_000, initialDelay = 600_000)
    public void aggregateToDB() {
        if (!"SIMULATED".equals(modeService.getCurrentMode())) return;

        log.info("[Aggregation] Flushing 10-minute averages to DB...");
        LocalDateTime now = LocalDateTime.now();

        tempStorage.forEach((location, values) -> {
            if (values.isEmpty()) return;

            List<Integer> snapshot;
            synchronized (values) {
                snapshot = new ArrayList<>(values);
                values.clear();
            }

            int avg = (int) Math.round(snapshot.stream()
                                                .mapToInt(Integer::intValue)
                                                .average()
                                                .orElse(0));

            CrowdData aggregated = CrowdData.builder()
                    .location(location)
                    .crowdCount(avg)
                    .status(calculateStatus(avg))
                    .source("Simulated")
                    .timestamp(now)
                    .build();

            crowdRepository.save(aggregated);
            log.info("[Aggregation] {} → avg={} saved (from {} samples)", location, avg, snapshot.size());
        });
    }

    // ── Daily cleanup: delete Simulated rows older than 7 days ───────────────

    /**
     * Runs once per day at 02:00 AM.
     * Deletes old "Simulated" rows to prevent unbounded table growth.
     * "Historical" rows (seeded 30-day baseline) are NEVER deleted.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldSimulatedData() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<CrowdData> old = crowdRepository.findSimulatedOlderThan(cutoff);
        if (!old.isEmpty()) {
            crowdRepository.deleteAll(old);
            log.info("[Cleanup] Deleted {} old Simulated rows (older than 7 days)", old.size());
        }
    }

    private String calculateStatus(int count) {
        if (count < 40)  return "LOW";
        if (count <= 80) return "MEDIUM";
        return "HIGH";
    }
}
