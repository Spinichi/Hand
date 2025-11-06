// Intervention.java
package com.finger.hand_backend.relief.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interventions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Intervention {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="intervention_code", nullable=false, length=50, unique=true)
    private String interventionCode;

    @Column(nullable=false, length=100)
    private String name;

    @Column(nullable=false, length=50) // 호흡법, 근육이완, 명상 등
    private String type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name="duration_seconds")
    private Integer durationSeconds;

    @Column(name="created_at")
    private LocalDateTime createdAt;
}

