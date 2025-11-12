package com.finger.hand_backend.counseling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 상담 분석 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounselingAnalysisResult {

    private Long reportId;
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer diaryCount;
    private String counselingAdvice;
    private LocalDateTime createdAt;
}
