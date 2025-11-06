// InterventionLog.java
package com.finger.hand_backend.relief.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "intervention_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InterventionLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="intervention_id", nullable=false)
    private Long interventionId;

    @Column(name="anomaly_detection_id")
    private Long anomalyDetectionId; // 자동추천 트리거가 있으면 연결

    @Enumerated(EnumType.STRING)
    @Column(name="trigger_type", nullable=false, length=20)
    private TriggerType triggerType; // AUTO_SUGGEST / MANUAL

    @Column(name="before_stress")
    private Integer beforeStress; // 점수(정수형으로 저장한 설계 유지)

    @Column(name="after_stress")
    private Integer afterStress;

    @Column(name="duration_seconds")
    private Integer durationSeconds;

    @Column(name="user_rating")
    private Integer userRating; // 1~5

    @Column(name="started_at")
    private LocalDateTime startedAt;

    @Column(name="ended_at")
    private LocalDateTime endedAt;

    @Column(name="gesture_code", length=50)
    private String gestureCode;

    @Column(name="created_at")
    private LocalDateTime createdAt;
}

