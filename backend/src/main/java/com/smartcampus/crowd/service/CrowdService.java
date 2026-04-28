package com.smartcampus.crowd.service;

import com.smartcampus.crowd.dto.CrowdRequest;
import com.smartcampus.crowd.dto.CrowdResponse;
import com.smartcampus.crowd.model.CrowdData;
import com.smartcampus.crowd.repository.CrowdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

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

        if ("SIMULATED".equals(mode)) {
            Integer inMemoryCrowd = simulationService.getCurrentCrowdMap().get(location);
            
            // Fallback to DB if simulation hasn't ticked
            if (inMemoryCrowd == null) {
                return crowdRepository.findTopByLocationAndSourceOrderByTimestampDesc(location, "Simulated")
                        .map(this::buildResponse)
                        .orElseGet(() -> buildWarmStartResponse(location));
            }

            return CrowdResponse.builder()
                    .location(location)
                    .count(inMemoryCrowd)
                    .status(calculateStatus(inMemoryCrowd))
                    .trend(simulationService.getLiveTrend(location)) // Use Live Trend
                    .bestTime(getBestTime(location))
                    .prediction(simulationService.getLivePrediction(location)) // Use Live Prediction
                    .waitingTime(calculateWaitingTime(location, inMemoryCrowd))
                    .lastUpdated(LocalDateTime.now())
                    .source("Simulated")
                    .build();
        }

        // Live mode fallback
        return crowdRepository.findTopByLocationOrderByTimestampDesc(location)
                .map(this::buildResponse)
                .orElseGet(() -> buildWarmStartResponse(location));
    }

    public int getPrediction(String location) {
        return simulationService.getLivePrediction(location);
    }



    public String getBestTime(String location) {
        List<Object[]> rows = crowdRepository.getBestHoursForLocation(location);
        if (rows == null || rows.isEmpty()) {
            rows = crowdRepository.getBestHoursForLocationAnyDay(location);
        }
        if (rows == null || rows.isEmpty()) return "09:00 - 10:00";

        int bestHour = ((Number) rows.get(0)[0]).intValue();
        return String.format("%02d:00 - %02d:00", bestHour, (bestHour + 1) % 24);
    }

    private CrowdResponse buildWarmStartResponse(String location) {
        int hour = LocalDateTime.now().getHour();
        Double avg = crowdRepository.getPredictionAvgAnyDay(location, hour);
        int count = (avg != null) ? (int) Math.round(avg) : 20;

        return CrowdResponse.builder()
                .location(location).count(count).status(calculateStatus(count))
                .trend("Stable").bestTime(getBestTime(location)).prediction(count)
                .waitingTime(calculateWaitingTime(location, count)).lastUpdated(LocalDateTime.now())
                .source("Historical").build();
    }

    private CrowdResponse buildResponse(CrowdData data) {
        return CrowdResponse.builder()
                .location(data.getLocation()).count(data.getCrowdCount()).status(data.getStatus())
                .trend(simulationService.getLiveTrend(data.getLocation())) // Live Trend even for DB rows
                .bestTime(getBestTime(data.getLocation()))
                .prediction(simulationService.getLivePrediction(data.getLocation())) // Live Prediction
                .waitingTime(calculateWaitingTime(data.getLocation(), data.getCrowdCount()))
                .lastUpdated(data.getTimestamp()).source(data.getSource()).build();
    }

    private int calculateWaitingTime(String location, int crowd) {
        // A. Fees Office (Queue-based)
        if ("Fees Office".equals(location)) {
            int wait = (int) Math.round(crowd * 1.5);
            return Math.min(60, wait);
        }

        // B. Library & Canteen (Capacity-based)
        int capacity = "Library".equals(location) ? 65 : 50;
        int turnoverTime = "Library".equals(location) ? 3 : 2;

        if (crowd <= capacity) {
            return 0;
        } else {
            return (crowd - capacity) * turnoverTime;
        }
    }

    private String calculateStatus(int count) {
        if (count < 40) return "LOW";
        if (count <= 80) return "MEDIUM";
        return "HIGH";
    }


}
