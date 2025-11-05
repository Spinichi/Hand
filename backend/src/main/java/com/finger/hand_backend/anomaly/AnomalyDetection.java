package com.finger.hand_backend.anomaly;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 이상치 탐지 엔티티
 * - stress_level >= 4 일 때 자동 생성
 * - Intervention 연결을 위한 이벤트 기록
 * - 최소 필드만 유지 (severity 등은 measurement 조인으로 해결)
 */
@Entity
@Table(
    name = "anomaly_detections",
    indexes = {
        @Index(name = "idx_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_measurement", columnList = "measurement_id")
    }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDetection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 측정 데이터 ID (FK)
     * - measurements 테이블 참조
     * - 조인으로 stress_level, stress_index 등 모든 정보 조회 가능
     */
    @Column(name = "measurement_id", nullable = false)
    private Long measurementId;

    /**
     * 감지 시각
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
