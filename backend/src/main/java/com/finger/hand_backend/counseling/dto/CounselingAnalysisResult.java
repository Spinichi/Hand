package com.finger.hand_backend.counseling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    // ========== 감정 그래프 데이터 ==========

    /**
     * 일별 다이어리 (감정 그래프용)
     * [{ date, longSummary, shortSummary, depressionScore }, ...]
     */
    private List<Map<String, Object>> dailyDiaries;

    // ========== AI 분석 결과 ==========

    /**
     * 관리자용 보고서
     */
    private String report;

    /**
     * 관리자용 조언
     */
    private String advice;

    // ========== 통계 ==========

    private Double averageDepressionScore;
    private Double maxDepressionScore;
    private Double minDepressionScore;

    // ========== 특별노트 ==========

    /**
     * 관리자가 작성한 팀원 특별 메모
     */
    private String specialNotes;

    // ========== 메타 ==========

    private LocalDateTime createdAt;

    /**
     * 하위 호환성을 위한 필드
     * @deprecated advice 필드 사용 권장
     */
    @Deprecated
    public String getCounselingAdvice() {
        return this.advice;
    }
}
