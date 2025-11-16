package com.finger.hand_backend.sleep;

import com.finger.hand_backend.common.dto.ApiResponse;
import com.finger.hand_backend.sleep.dto.SleepCreateResponse;
import com.finger.hand_backend.sleep.dto.SleepRequest;
import com.finger.hand_backend.sleep.dto.SleepResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Sleep Controller
 * - 수면 데이터 CRUD API
 */
@RestController
@RequestMapping("/sleep")
@RequiredArgsConstructor
public class SleepController {

    private final SleepService sleepService;

    /**
     * 수면 데이터 저장
     * - 수면 시작/종료 시간을 받아서 자동으로 수면 시간 계산
     * - 같은 날짜에 이미 데이터가 있으면 업데이트
     *
     * @param authentication 인증 정보
     * @param request        수면 데이터 요청
     * @return 저장된 수면 데이터 ID 및 수면 시간
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SleepCreateResponse>> createSleep(
        Authentication authentication,
        @Valid @RequestBody SleepRequest request
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Sleep sleep = sleepService.save(userId, request);

        SleepCreateResponse data = SleepCreateResponse.builder()
            .id(sleep.getId())
            .sleepDurationMinutes(sleep.getSleepDurationMinutes())
            .sleepDurationHours(sleep.getSleepDurationMinutes() / 60)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(data, "수면 데이터가 저장되었습니다"));
    }

    /**
     * 오늘의 수면 데이터 조회
     *
     * @param authentication 인증 정보
     * @return 오늘의 수면 데이터 (없으면 null)
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<SleepResponse>> getTodaySleep(
        Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Sleep sleep = sleepService.getTodaySleep(userId);

        if (sleep == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "오늘의 수면 데이터가 없습니다"));
        }

        SleepResponse data = SleepResponse.from(sleep);

        return ResponseEntity.ok(ApiResponse.success(data, "오늘의 수면 데이터를 조회했습니다"));
    }

    /**
     * 특정 날짜의 수면 데이터 조회
     *
     * @param authentication 인증 정보
     * @param date           조회 날짜 (예: 2025-01-14)
     * @return 수면 데이터 (없으면 null)
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<SleepResponse>> getSleepByDate(
        Authentication authentication,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Sleep sleep = sleepService.getSleepByDate(userId, date);

        if (sleep == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "해당 날짜의 수면 데이터가 없습니다"));
        }

        SleepResponse data = SleepResponse.from(sleep);

        return ResponseEntity.ok(ApiResponse.success(data, "수면 데이터를 조회했습니다"));
    }

    /**
     * 사용자의 모든 수면 데이터 조회 (최신순)
     *
     * @param authentication 인증 정보
     * @return 수면 데이터 리스트
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SleepResponse>>> getAllSleep(
        Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        List<Sleep> sleeps = sleepService.getAllSleep(userId);

        List<SleepResponse> data = sleeps.stream()
            .map(SleepResponse::from)
            .toList();

        return ResponseEntity.ok(ApiResponse.success(data, "수면 데이터 목록을 조회했습니다"));
    }

    /**
     * 특정 기간의 수면 데이터 조회
     *
     * @param authentication 인증 정보
     * @param startDate      시작 날짜
     * @param endDate        종료 날짜
     * @return 수면 데이터 리스트
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<SleepResponse>>> getSleepByDateRange(
        Authentication authentication,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Long userId = Long.valueOf(authentication.getName());

        List<Sleep> sleeps = sleepService.getSleepByDateRange(userId, startDate, endDate);

        List<SleepResponse> data = sleeps.stream()
            .map(SleepResponse::from)
            .toList();

        return ResponseEntity.ok(ApiResponse.success(data, "기간별 수면 데이터를 조회했습니다"));
    }

    /**
     * 수면 데이터 삭제
     *
     * @param authentication 인증 정보
     * @param id             수면 데이터 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSleep(
        Authentication authentication,
        @PathVariable Long id
    ) {
        Long userId = Long.valueOf(authentication.getName());

        sleepService.deleteSleep(userId, id);

        return ResponseEntity.noContent().build();
    }
}