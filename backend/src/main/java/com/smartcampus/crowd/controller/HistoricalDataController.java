package com.smartcampus.crowd.controller;

import com.smartcampus.crowd.service.HistoricalDataGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/historical")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class HistoricalDataController {

    private final HistoricalDataGenerator generator;

    @PostMapping("/generate")
    public ResponseEntity<String> generateHistory() {
        generator.generateHistoricalData();
        return ResponseEntity.ok("Historical data generation started for the past 60 days.");
    }
}
