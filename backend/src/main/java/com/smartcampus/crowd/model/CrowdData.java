package com.smartcampus.crowd.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crowd_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrowdData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String location;
    
    @Column(name = "crowd_count", nullable = false)
    private int crowdCount;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private String source; // Simulated or Historical
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
