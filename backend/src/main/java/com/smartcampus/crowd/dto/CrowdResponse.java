package com.smartcampus.crowd.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CrowdResponse {
    private String location;
    private int count;
    private String status;
    private String trend;
    private String bestTime;
    private int prediction;
    private int waitingTime;
    private LocalDateTime lastUpdated;
    private String source;
}
