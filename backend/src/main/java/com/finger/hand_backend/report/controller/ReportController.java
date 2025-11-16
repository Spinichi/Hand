package com.finger.hand_backend.report.controller;

import com.finger.hand_backend.common.dto.ApiResponse;
import com.finger.hand_backend.report.entity.MonthlyReport;
import com.finger.hand_backend.report.entity.MonthlyReportDetail;
import com.finger.hand_backend.report.entity.WeeklyReport;
import com.finger.hand_backend.report.entity.WeeklyReportDetail;
import com.finger.hand_backend.report.service.MonthlyReportService;
import com.finger.hand_backend.report.service.ReportGenerationResult;
import com.finger.hand_backend.report.service.WeeklyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 보고서 컨트롤러
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final WeeklyReportService weeklyReportService;
    private final MonthlyReportService monthlyReportService;

    // ========== 주간 보고서 ==========

    /**
     * 주간 보고서 생성
     */
    @PostMapping("/weekly")
    public ResponseEntity<ApiResponse<Long>> generateWeeklyReport(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long userId = Long.valueOf(authentication.getName());

        if (date == null) {
            date = LocalDate.now();
        }

        log.info("POST /reports/weekly - userId: {}, date: {}", userId, date);

        try {
            ReportGenerationResult result = weeklyReportService.generateWeeklyReport(userId, date);

            if (result.isNewlyCreated()) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(result.getReportId(), "주간 보고서가 생성되었습니다."));
            } else {
                return ResponseEntity.ok()
                        .body(ApiResponse.success(result.getReportId(), "이미 생성된 주간 보고서입니다."));
            }

        } catch (IllegalStateException e) {
            log.warn("Failed to generate weekly report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }

    /**
     * 주간 보고서 조회
     */
    @GetMapping("/weekly/{reportId}")
    public ResponseEntity<ApiResponse<WeeklyReportDetail>> getWeeklyReport(
            Authentication authentication,
            @PathVariable Long reportId) {

        Long userId = Long.valueOf(authentication.getName());

        log.info("GET /reports/weekly/{} - userId: {}", reportId, userId);
        WeeklyReportDetail detail = weeklyReportService.getWeeklyReportDetail(userId, reportId);
        return ResponseEntity.ok(ApiResponse.success(detail, "주간 보고서 조회 성공"));
    }

    /**
     * 최신 주간 보고서 조회
     */
    @GetMapping("/weekly/latest")
    public ResponseEntity<ApiResponse<WeeklyReportDetail>> getLatestWeeklyReport(
            Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());

        log.info("GET /reports/weekly/latest - userId: {}", userId);
        return weeklyReportService.getLatestWeeklyReport(userId)
                .map(detail -> ResponseEntity.ok(ApiResponse.success(detail, "최신 주간 보고서 조회 성공")))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail("보고서를 찾을 수 없습니다.")));
    }

    /**
     * 주간 보고서 목록 조회 (페이징)
     */
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<Page<WeeklyReport>>> getWeeklyReports(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {  // Repository 메서드에 정렬이 이미 포함됨

        Long userId = Long.valueOf(authentication.getName());

        log.info("GET /reports/weekly - userId: {}", userId);
        Page<WeeklyReport> reports = weeklyReportService.getWeeklyReports(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(reports, "주간 보고서 목록 조회 성공"));
    }

    // ========== 월간 보고서 ==========

    /**
     * 월간 보고서 생성
     */
    @PostMapping("/monthly")
    public ResponseEntity<ApiResponse<Long>> generateMonthlyReport(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long userId = Long.valueOf(authentication.getName());

        if (date == null) {
            date = LocalDate.now();
        }

        log.info("POST /reports/monthly - userId: {}, date: {}", userId, date);

        try {
            ReportGenerationResult result = monthlyReportService.generateMonthlyReport(userId, date);

            if (result.isNewlyCreated()) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(result.getReportId(), "월간 보고서가 생성되었습니다."));
            } else {
                return ResponseEntity.ok()
                        .body(ApiResponse.success(result.getReportId(), "이미 생성된 월간 보고서입니다."));
            }

        } catch (IllegalStateException e) {
            log.warn("Failed to generate monthly report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }

    /**
     * 월간 보고서 조회
     */
    @GetMapping("/monthly/{reportId}")
    public ResponseEntity<ApiResponse<MonthlyReportDetail>> getMonthlyReport(
            Authentication authentication,
            @PathVariable Long reportId) {

        Long userId = Long.valueOf(authentication.getName());

        log.info("GET /reports/monthly/{} - userId: {}", reportId, userId);
        MonthlyReportDetail detail = monthlyReportService.getMonthlyReportDetail(userId, reportId);
        return ResponseEntity.ok(ApiResponse.success(detail, "월간 보고서 조회 성공"));
    }

    /**
     * 최신 월간 보고서 조회
     */
    @GetMapping("/monthly/latest")
    public ResponseEntity<ApiResponse<MonthlyReportDetail>> getLatestMonthlyReport(
            Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());

        log.info("GET /reports/monthly/latest - userId: {}", userId);
        return monthlyReportService.getLatestMonthlyReport(userId)
                .map(detail -> ResponseEntity.ok(ApiResponse.success(detail, "최신 월간 보고서 조회 성공")))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail("보고서를 찾을 수 없습니다.")));
    }

    /**
     * 월간 보고서 목록 조회 (페이징)
     */
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<Page<MonthlyReport>>> getMonthlyReports(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {  // Repository 메서드에 정렬이 이미 포함됨

        Long userId = Long.valueOf(authentication.getName());

        log.info("GET /reports/monthly - userId: {}", userId);
        Page<MonthlyReport> reports = monthlyReportService.getMonthlyReports(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(reports, "월간 보고서 목록 조회 성공"));
    }
}

