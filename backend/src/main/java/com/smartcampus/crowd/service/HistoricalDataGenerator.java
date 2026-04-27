package com.smartcampus.crowd.service;

import com.smartcampus.crowd.model.CrowdData;
import com.smartcampus.crowd.repository.CrowdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoricalDataGenerator {

    private final CrowdRepository crowdRepository;
    private final Random random = new Random();

    private static final Map<String, Integer> LOCATIONS = Map.of(
        "Library", 40,
        "Canteen", 60,
        "Admin Office", 35,
        "Fees Office", 30,
        "Exam Branch", 25
    );

    private static final Map<Integer, Integer> HOURLY_BASE = Map.of(
        9, 30, 10, 50, 11, 70, 12, 100, 13, 110, 14, 95, 15, 80, 16, 60, 17, 40, 18, 25
    );

    @PostConstruct
    public void init() {
        LOCATIONS.keySet().forEach(location -> {
            if (crowdRepository.countByLocationAndSource(location, "Historical") == 0) {
                generateHistoricalForLocation(location);
            }
        });
    }

    public void generateHistoricalForLocation(String location) {
        log.info("Generating granular historical data for {}: 6 months, every 10 mins...", location);
        List<CrowdData> allData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int dayOffset = 180; dayOffset >= 0; dayOffset--) {
            LocalDateTime dayDate = now.minusDays(dayOffset);
            DayOfWeek dayOfWeek = dayDate.getDayOfWeek();
            double dayMultiplier = getDayMultiplier(dayOfWeek);

            for (int hour = 9; hour <= 18; hour++) {
                int baseValue = HOURLY_BASE.getOrDefault(hour, 20);
                double locMultiplier = getLocationMultiplier(location, hour);

                for (int min = 0; min < 60; min += 10) {
                    LocalDateTime timestamp = dayDate.withHour(hour).withMinute(min).withSecond(0).withNano(0);
                    
                    // Simple random noise ±10%
                    double noiseFactor = 0.9 + (random.nextDouble() * 0.2);
                    
                    int finalCount = (int) (baseValue * dayMultiplier * locMultiplier * noiseFactor);
                    
                    // Specific logic for Exam Branch random spikes (1.3x at 15% probability)
                    if ("Exam Branch".equals(location) && random.nextDouble() < 0.15) {
                        finalCount = (int) (finalCount * 1.3);
                    }

                    allData.add(CrowdData.builder()
                        .location(location)
                        .crowdCount(Math.max(0, finalCount))
                        .status(calculateStatus(finalCount))
                        .source("Historical")
                        .timestamp(timestamp)
                        .build());
                }
            }
            
            // Batch save every day to avoid memory issues and long transactions
            if (!allData.isEmpty()) {
                crowdRepository.saveAll(allData);
                allData.clear();
            }
        }
        log.info("Successfully generated historical records for {}.", location);
    }

    private double getDayMultiplier(DayOfWeek day) {
        if (day == DayOfWeek.SATURDAY) return 0.5;
        if (day == DayOfWeek.SUNDAY) return 0.3;
        if (day == DayOfWeek.MONDAY || day == DayOfWeek.FRIDAY) return 1.1;
        return 1.0; // Tue, Wed, Thu
    }

    private double getLocationMultiplier(String location, int hour) {
        switch (location) {
            case "Library":
                return (hour >= 12 && hour <= 14) ? 1.3 : 1.0;
            case "Canteen":
                return (hour >= 13 && hour <= 15) ? 1.6 : 1.0;
            case "Admin Office":
                return (hour >= 11 && hour <= 14) ? 1.3 : 1.0;
            case "Fees Office":
                return (hour >= 10 && hour <= 12) ? 1.4 : 1.0;
            default:
                return 1.0;
        }
    }

    private String calculateStatus(int count) {
        if (count < 40) return "LOW";
        if (count <= 80) return "MEDIUM";
        return "HIGH";
    }

    public void generateHistoricalData() {
        log.info("Triggering historical data generation for all locations...");
        LOCATIONS.keySet().forEach(this::generateHistoricalForLocation);
    }
}
