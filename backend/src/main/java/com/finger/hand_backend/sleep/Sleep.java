package com.finger.hand_backend.sleep;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 수면 데이터 엔티티
 * - 사용자의 수면 시작/종료 시간 기록
 * - 수면 시간 자동 계산
 */
@Entity
@Table(
    name = "sleep_records",
    indexes = {
        @Index(name = "idx_user_sleep_date", columnList = "user_id, sleep_date")
    }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sleep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 수면 시작 시간
     */
    @Column(name = "sleep_start_time", nullable = false)
    private LocalDateTime sleepStartTime;

    /**
     * 수면 종료 시간
     */
    @Column(name = "sleep_end_time", nullable = false)
    private LocalDateTime sleepEndTime;

    /**
     * 수면 시간 (분 단위)
     * sleepEndTime - sleepStartTime 자동 계산
     */
    @Column(name = "sleep_duration_minutes", nullable = false)
    private Integer sleepDurationMinutes;

    /**
     * 수면 날짜 (조회 편의성)
     * sleepStartTime의 날짜
     */
    @Column(name = "sleep_date", nullable = false)
    private LocalDate sleepDate;

    /**
     * 생성 시각 (서버에 저장된 시간)
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수면 시간 계산 (분 단위)
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @return 수면 시간 (분)
     */
    public static int calculateDurationMinutes(LocalDateTime startTime, LocalDateTime endTime) {
        return (int) ChronoUnit.MINUTES.between(startTime, endTime);
    }

    /**
     * 수면 날짜 추출
     * @param startTime 시작 시간
     * @return 수면 날짜
     */
    public static LocalDate extractSleepDate(LocalDateTime startTime) {
        return startTime.toLocalDate();
    }

    /**
     * PrePersist 훅: 저장 전 수면 시간 및 날짜 자동 계산
     */
    @PrePersist
    public void prePersist() {
        if (sleepDurationMinutes == null && sleepStartTime != null && sleepEndTime != null) {
            this.sleepDurationMinutes = calculateDurationMinutes(sleepStartTime, sleepEndTime);
        }
        if (sleepDate == null && sleepStartTime != null) {
            this.sleepDate = extractSleepDate(sleepStartTime);
        }
    }
}