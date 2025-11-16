package com.finger.hand_backend.counseling.controller;

import com.finger.hand_backend.common.dto.ApiResponse;
import com.finger.hand_backend.counseling.dto.CounselingAnalyzeRequest;
import com.finger.hand_backend.counseling.dto.CounselingAnalysisResult;
import com.finger.hand_backend.counseling.service.CounselingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 상담 컨트롤러
 */
@RestController
@RequestMapping("/manager/counseling")
@RequiredArgsConstructor
@Slf4j
public class ManagerCounselingController {

    private final CounselingService counselingService;

    private Long userId(Authentication auth) {
        return Long.valueOf(auth.getName());
    }

    /**
     * 관리자 상담용 분석 (새로 생성)
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<CounselingAnalysisResult>> analyzeCounseling(
            Authentication auth,
            @RequestBody CounselingAnalyzeRequest request) {

        Long managerId = userId(auth);
        log.info("POST /manager/counseling/analyze - managerId: {}, groupId: {}, userId: {}, period: {} ~ {}",
                managerId, request.getGroupId(), request.getUserId(), request.getStartDate(), request.getEndDate());

        try {
            CounselingAnalysisResult result = counselingService.analyzeCounseling(
                    managerId,
                    request.getGroupId(),
                    request.getUserId(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            return ResponseEntity.ok(ApiResponse.success(result, "상담 분석이 완료되었습니다."));

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Failed to analyze counseling: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }

    /**
     * 상담 보고서 목록 조회 (특정 유저)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CounselingAnalysisResult>>> getCounselingReports(
            Authentication auth,
            @RequestParam Long groupId,
            @RequestParam Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long managerId = userId(auth);
        log.info("GET /manager/counseling - managerId: {}, groupId: {}, userId: {}, page: {}",
                managerId, groupId, userId, pageable.getPageNumber());

        try {
            Page<CounselingAnalysisResult> reports = counselingService.getCounselingReports(managerId, groupId, userId, pageable);
            return ResponseEntity.ok(ApiResponse.success(reports, "상담 보고서 목록을 조회했습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("Access denied or report not found: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to get counseling reports", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("상담 보고서 목록 조회에 실패했습니다."));
        }
    }

    /**
     * 상담 보고서 상세 조회
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<CounselingAnalysisResult>> getCounselingReport(
            Authentication auth,
            @PathVariable Long reportId,
            @RequestParam Long groupId) {

        Long managerId = userId(auth);
        log.info("GET /manager/counseling/{} - managerId: {}, groupId: {}", reportId, managerId, groupId);

        try {
            CounselingAnalysisResult report = counselingService.getCounselingReport(managerId, groupId, reportId);
            return ResponseEntity.ok(ApiResponse.success(report, "상담 보고서를 조회했습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("Counseling report not found or access denied: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }

    /**
     * 최신 상담 보고서 조회
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<CounselingAnalysisResult>> getLatestCounselingReport(
            Authentication auth,
            @RequestParam Long groupId,
            @RequestParam Long userId) {

        Long managerId = userId(auth);
        log.info("GET /manager/counseling/latest - managerId: {}, groupId: {}, userId: {}", managerId, groupId, userId);

        try {
            CounselingAnalysisResult report = counselingService.getLatestCounselingReport(managerId, groupId, userId);
            return ResponseEntity.ok(ApiResponse.success(report, "최신 상담 보고서를 조회했습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("Latest counseling report not found or access denied: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }
}
