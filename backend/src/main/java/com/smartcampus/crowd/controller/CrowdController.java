package com.smartcampus.crowd.controller;

import com.smartcampus.crowd.dto.CrowdRequest;
import com.smartcampus.crowd.dto.CrowdResponse;
import com.smartcampus.crowd.model.CrowdData;
import com.smartcampus.crowd.service.CrowdService;
import com.smartcampus.crowd.service.SystemModeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/crowd")
@CrossOrigin(origins = "*")
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



    @GetMapping("/best-time")
    public String getBestTime(@RequestParam String location) {
        return crowdService.getBestTime(location);
    }

    @GetMapping("/prediction")
    public int getPrediction(@RequestParam String location) {
        return crowdService.getPrediction(location);
    }
}
