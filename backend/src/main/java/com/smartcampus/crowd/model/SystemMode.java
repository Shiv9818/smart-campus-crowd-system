package com.smartcampus.crowd.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_mode")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemMode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String mode; // SIMULATED or LIVE
}
