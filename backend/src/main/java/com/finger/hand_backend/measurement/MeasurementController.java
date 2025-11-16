package com.finger.hand_backend.measurement;

import com.finger.hand_backend.common.dto.ApiResponse;
import com.finger.hand_backend.measurement.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.List;

/**
 * Measurement Controller
 * - 측정 데이터 CRUD API
 */
@RestController
@RequestMapping("/measurements")
@RequiredArgsConstructor
public class MeasurementController {

    private final MeasurementService measurementService;

    /**
     * 측정 데이터 저장
     *
     * @param authentication 인증 정보
     * @param request        측정 데이터 요청
     * @return 저장된 측정 데이터 ID
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MeasurementCreateResponse>> createMeasurement(
            Authentication authentication,
            @Valid @RequestBody MeasurementRequest request
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Measurement measurement = measurementService.save(userId, request);

        MeasurementCreateResponse data = MeasurementCreateResponse.builder()
                .id(measurement.getId())
                .stressIndex(measurement.getStressIndex())
                .stressLevel(measurement.getStressLevel())
                .isAnomaly(measurement.getIsAnomaly())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, "측정 데이터가 저장되었습니다"));
    }

    /**
     * 내 측정 데이터 목록 조회 (페이징)
     *
     * @param authentication 인증 정보
     * @param pageable       페이징 정보 (기본: 20개, 최신순)
     * @return 측정 데이터 페이지
     */
    @GetMapping
    public ResponseEntity<ApiResponse<MeasurementPageResponse>> getMyMeasurements(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "measuredAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Page<Measurement> measurementPage = measurementService.getMyMeasurements(userId, pageable);

        MeasurementPageResponse data = MeasurementPageResponse.builder()
                .total(measurementPage.getTotalElements())
                .page(measurementPage.getNumber())
                .size(measurementPage.getSize())
                .measurements(measurementPage.getContent().stream()
                        .map(MeasurementResponse::from)
                        .toList())
                .build();

        return ResponseEntity.ok(ApiResponse.success(data, "측정 데이터 목록을 조회했습니다"));
    }

    /**
     * 특정 기간 측정 데이터 조회
     *
     * @param authentication 인증 정보
     * @param startDate      시작 날짜 (예: 2025-01-10)
     * @param endDate        종료 날짜 (예: 2025-01-12)
     * @return 측정 데이터 리스트
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<MeasurementRangeResponse>> getMeasurementsByDateRange(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Long userId = Long.valueOf(authentication.getName());

        List<Measurement> measurements = measurementService
                .getMeasurementsByDateRange(userId, startDate, endDate);

        MeasurementRangeResponse data = MeasurementRangeResponse.builder()
                .total(measurements.size())
                .startDate(startDate)
                .endDate(endDate)
                .measurements(measurements.stream()
                        .map(MeasurementResponse::from)
                        .toList())
                .build();

        return ResponseEntity.ok(ApiResponse.success(data, "기간별 측정 데이터를 조회했습니다"));
    }

    /**
     * 특정 측정 데이터 조회
     *
     * @param authentication 인증 정보
     * @param id             측정 데이터 ID
     * @return 측정 데이터 상세
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MeasurementResponse>> getMeasurement(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Measurement measurement = measurementService.getMeasurement(userId, id);

        return ResponseEntity.ok(ApiResponse.success(MeasurementResponse.from(measurement), "상세 데이터 조회"));
    }

    /**
     * 측정 데이터 삭제
     *
     * @param authentication 인증 정보
     * @param id             측정 데이터 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeasurement(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(authentication.getName());

        measurementService.deleteMeasurement(userId, id);

        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 기간 측정 데이터 개수 조회
     *
     * @param authentication 인증 정보
     * @param startDate      시작 날짜
     * @param endDate        종료 날짜
     * @return 측정 데이터 개수
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<MeasurementCountResponse>> countMeasurements(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Long userId = Long.valueOf(authentication.getName());

        long count = measurementService.countMeasurements(userId, startDate, endDate);

        MeasurementCountResponse data = MeasurementCountResponse.builder()
                .count(count)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return ResponseEntity.ok(ApiResponse.success(data, "측정 데이터 개수를 조회했습니다"));
    }

    /**
     * 특정 날짜의 이상치 조회
     *
     * @param authentication 인증 정보
     * @param date           조회 날짜 (선택, 기본값: 오늘)
     * @return 해당 날짜의 이상치 데이터
     */
    @GetMapping("/anomalies/daily")
    public ResponseEntity<ApiResponse<DailyAnomalyResponse>> getDailyAnomalies(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Long userId = Long.valueOf(authentication.getName());
        LocalDate targetDate = date != null ? date : LocalDate.now();

        DailyAnomalyResponse data = measurementService.getDailyAnomalies(userId, targetDate);

        return ResponseEntity.ok(ApiResponse.success(data, "일일 이상치 데이터를 조회했습니다"));
    }

    /**
     * 최근 일주일간 이상치 조회 (날짜별 그룹화)
     *
     * @param authentication 인증 정보
     * @return 일주일간 날짜별 이상치 데이터
     */
    @GetMapping("/anomalies/weekly")
    public ResponseEntity<ApiResponse<WeeklyAnomalyResponse>> getWeeklyAnomalies(
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        WeeklyAnomalyResponse data = measurementService.getWeeklyAnomalies(userId);

        return ResponseEntity.ok(ApiResponse.success(data, "주간 이상치 데이터를 조회했습니다"));
    }

    /**
     * 가장 최근 측정 데이터 조회
     * 홈 화면 표시용 (BPM, 스트레스 레벨 등)
     *
     * @param authentication 인증 정보
     * @return 최근 측정 데이터 (없으면 null)
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<MeasurementResponse>> getLatestMeasurement(
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Measurement latest = measurementService.getLatestMeasurement(userId);

        if (latest == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "측정 데이터가 없습니다"));
        }

        MeasurementResponse data = MeasurementResponse.from(latest);

        return ResponseEntity.ok(ApiResponse.success(data, "최근 측정 데이터를 조회했습니다"));
    }
}
