package com.finger.hand_backend.counseling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 상담 보고서 (MySQL)
 */
@Entity
@Table(name = "counseling_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounselingReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer diaryCount;

    /**
     * MongoDB 상세 보고서 ID
     */
    @Column(nullable = false)
    private String mongodbReportId;

    /**
     * 상담 조언 (긴 텍스트) - 하위 호환성 유지
     * @deprecated MongoDB의 CounselingReportDetail 사용 권장
     */
    @Deprecated
    @Column(columnDefinition = "TEXT")
    private String counselingAdvice;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
