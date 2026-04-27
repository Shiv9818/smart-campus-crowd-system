package com.smartcampus.crowd.dto;

import lombok.Data;

@Data
public class CrowdRequest {
    private String location;
    private int crowdCount;
    private String source;
}
