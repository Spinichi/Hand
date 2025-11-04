package com.finger.hand_backend.measurement;

import com.finger.hand_backend.measurement.dto.MeasurementRequest;
import com.finger.hand_backend.measurement.dto.MeasurementResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> createMeasurement(
            Authentication authentication,
            @Valid @RequestBody MeasurementRequest request
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Measurement measurement = measurementService.save(userId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("id", measurement.getId());
        response.put("stressIndex", measurement.getStressIndex());
        response.put("stressLevel", measurement.getStressLevel());
        response.put("message", "측정 데이터가 저장되었습니다");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 측정 데이터 목록 조회 (페이징)
     *
     * @param authentication 인증 정보
     * @param pageable       페이징 정보 (기본: 20개, 최신순)
     * @return 측정 데이터 페이지
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyMeasurements(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "measuredAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Page<Measurement> measurementPage = measurementService.getMyMeasurements(userId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("total", measurementPage.getTotalElements());
        response.put("page", measurementPage.getNumber());
        response.put("size", measurementPage.getSize());
        response.put("measurements", measurementPage.getContent().stream()
                .map(MeasurementResponse::from)
                .toList());

        return ResponseEntity.ok(response);
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
    public ResponseEntity<Map<String, Object>> getMeasurementsByDateRange(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Long userId = Long.valueOf(authentication.getName());

        List<Measurement> measurements = measurementService
                .getMeasurementsByDateRange(userId, startDate, endDate);

        Map<String, Object> response = new HashMap<>();
        response.put("total", measurements.size());
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("measurements", measurements.stream()
                .map(MeasurementResponse::from)
                .toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 측정 데이터 조회
     *
     * @param authentication 인증 정보
     * @param id             측정 데이터 ID
     * @return 측정 데이터 상세
     */
    @GetMapping("/{id}")
    public ResponseEntity<MeasurementResponse> getMeasurement(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Measurement measurement = measurementService.getMeasurement(userId, id);

        return ResponseEntity.ok(MeasurementResponse.from(measurement));
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
    public ResponseEntity<Map<String, Object>> countMeasurements(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Long userId = Long.valueOf(authentication.getName());

        long count = measurementService.countMeasurements(userId, startDate, endDate);

        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        response.put("startDate", startDate);
        response.put("endDate", endDate);

        return ResponseEntity.ok(response);
    }
}
