package com.smartcampus.crowd.controller;

import com.smartcampus.crowd.dto.CrowdRequest;
import com.smartcampus.crowd.dto.CrowdResponse;
import com.smartcampus.crowd.model.CrowdData;
import com.smartcampus.crowd.service.CrowdService;
import com.smartcampus.crowd.service.SystemModeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/crowd")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class CrowdController {


    private final CrowdService crowdService;
    private final SystemModeService modeService;

    @PostMapping("/update")
    public CrowdData updateCrowd(@RequestBody CrowdRequest request) {
        if (!"LIVE".equals(modeService.getCurrentMode())) {
            throw new RuntimeException("System is not in LIVE mode. Data update ignored.");
        }
        return crowdService.updateCrowdData(request);
    }

    @GetMapping("/current")
    public CrowdResponse getCurrent(@RequestParam String location) {
        return crowdService.getCurrentData(location);
    }

    @GetMapping("/historical")
    public CrowdResponse getHistorical(@RequestParam String location) {
        return crowdService.getHistoricalAnalytics(location);
    }

    @GetMapping("/history")
    public Map<String, List<?>> getHistory(@RequestParam String location) {
        List<CrowdData> history = crowdService.getHistory(location);
        return Map.of(
            "times", history.stream().map(d -> d.getTimestamp().toString()).collect(Collectors.toList()),
            "counts", history.stream().map(CrowdData::getCrowdCount).collect(Collectors.toList())
        );
    }

    @GetMapping("/trend")
    public List<Map<String, Object>> getTrend(@RequestParam String location) {
        return crowdService.getHistoricalHourlyTrend(location);
    }

    @GetMapping("/best-time")
    public String getBestTime(@RequestParam String location) {
        return crowdService.getBestTime(location);
    }

    @GetMapping("/prediction")
    public int getPrediction(@RequestParam String location) {
        return crowdService.getPrediction(location);
    }
}
