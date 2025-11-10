package com.finger.hand_backend.report.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 보고서 생성 결과
 */
@Getter
@AllArgsConstructor
public class ReportGenerationResult {

    private final Long reportId;
    private final boolean isNewlyCreated;

    /**
     * 새로 생성된 보고서
     */
    public static ReportGenerationResult created(Long reportId) {
        return new ReportGenerationResult(reportId, true);
    }

    /**
     * 이미 존재하는 보고서
     */
    public static ReportGenerationResult existing(Long reportId) {
        return new ReportGenerationResult(reportId, false);
    }
}
