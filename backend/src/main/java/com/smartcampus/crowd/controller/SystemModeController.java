package com.smartcampus.crowd.controller;

import com.smartcampus.crowd.service.SystemModeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mode")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class SystemModeController {

    private final SystemModeService service;

    @GetMapping("/current")
    public String getCurrentMode() {
        return service.getCurrentMode();
    }

    @PostMapping("/simulated")
    public void setSimulatedMode() {
        service.setMode("SIMULATED");
    }

    @PostMapping("/live")
    public void setLiveMode() {
        service.setMode("LIVE");
    }
}
