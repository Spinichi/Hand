package com.finger.hand_backend.anomaly;

import com.finger.hand_backend.anomaly.dto.AnomalyDetectionPageResponse;
import com.finger.hand_backend.anomaly.dto.AnomalyDetectionResponse;
import com.finger.hand_backend.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AnomalyDetection Controller
 * - 이상치 이력 조회 및 관리
 */
@RestController
@RequestMapping("/anomalies")
@RequiredArgsConstructor
public class AnomalyDetectionController {

    private final AnomalyDetectionService anomalyDetectionService;

    /**
     * 내 이상치 목록 조회 (페이징)
     *
     * @param authentication 인증 정보
     * @param pageable       페이징 정보 (기본: 20개, 최신순)
     * @return 이상치 페이지
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AnomalyDetectionPageResponse>> getMyAnomalies(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Page<AnomalyDetectionResponse> anomalyPage = anomalyDetectionService.getMyAnomalies(userId, pageable);

        AnomalyDetectionPageResponse data = AnomalyDetectionPageResponse.builder()
                .total(anomalyPage.getTotalElements())
                .page(anomalyPage.getNumber())
                .size(anomalyPage.getSize())
                .anomalies(anomalyPage.getContent())
                .build();

        return ResponseEntity.ok(ApiResponse.success(data, "이상치 목록을 조회했습니다"));
    }

    /**
     * 특정 이상치 조회
     *
     * @param authentication 인증 정보
     * @param id             이상치 ID
     * @return 이상치 상세
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnomalyDetectionResponse>> getAnomaly(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(authentication.getName());

        AnomalyDetectionResponse data = anomalyDetectionService.getAnomalyById(userId, id);

        return ResponseEntity.ok(ApiResponse.success(data, "이상치를 조회했습니다"));
    }

    /**
     * 특정 기간 이상치 조회
     *
     * @param authentication 인증 정보
     * @param start          시작 시각
     * @param end            종료 시각
     * @return 이상치 리스트
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<AnomalyDetectionResponse>>> getAnomaliesByDateRange(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        Long userId = Long.valueOf(authentication.getName());

        List<AnomalyDetectionResponse> data = anomalyDetectionService
                .getAnomaliesByDateRange(userId, start, end);

        return ResponseEntity.ok(ApiResponse.success(data, "기간별 이상치를 조회했습니다"));
    }

    /**
     * 특정 기간 이상치 개수 조회
     *
     * @param authentication 인증 정보
     * @param start          시작 시각
     * @param end            종료 시각
     * @return 개수
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countAnomalies(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        Long userId = Long.valueOf(authentication.getName());

        long count = anomalyDetectionService.countAnomalies(userId, start, end);

        return ResponseEntity.ok(ApiResponse.success(count, "이상치 개수를 조회했습니다"));
    }

    /**
     * 이상치 삭제
     *
     * @param authentication 인증 정보
     * @param id             이상치 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnomaly(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(authentication.getName());

        anomalyDetectionService.deleteAnomaly(userId, id);

        return ResponseEntity.noContent().build();
    }
}
