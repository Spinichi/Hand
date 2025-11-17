package com.finger.hand_backend.risk;

import com.finger.hand_backend.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 일일 위험 점수 Controller
 */
@RestController
@RequestMapping("/risk")
@RequiredArgsConstructor
public class DailyRiskScoreController {

    private final DailyRiskScoreService riskScoreService;

    /**
     * 오늘 점수 존재 여부 조회
     * GET /risk/today/exists
     *
     * @param auth 인증 정보
     * @return 오늘 점수 존재 여부
     */
    @GetMapping("/today/exists")
    public ResponseEntity<ApiResponse<TodayScoreExistsResponse>> checkTodayScoreExists(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        boolean exists = riskScoreService.hasTodayScore(userId);

        return ResponseEntity.ok(ApiResponse.success(
                new TodayScoreExistsResponse(exists),
                exists ? "오늘의 점수가 존재합니다" : "오늘의 점수가 없습니다"
        ));
    }

    /**
     * 오늘 점수 조회
     * GET /risk/today
     *
     * @param auth 인증 정보
     * @return 오늘의 위험 점수 정보
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayScoreResponse>> getTodayScore(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        DailyRiskScore todayScore = riskScoreService.getTodayScore(userId);

        if (todayScore == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "오늘의 점수가 없습니다"));
        }

        TodayScoreResponse response = TodayScoreResponse.from(todayScore);
        return ResponseEntity.ok(ApiResponse.success(response, "오늘의 점수를 조회했습니다"));
    }

    // ===== DTOs =====

    /**
     * 오늘 점수 존재 여부 응답
     */
    public record TodayScoreExistsResponse(boolean exists) {}

    /**
     * 오늘 점수 조회 응답
     */
    public record TodayScoreResponse(
            Long id,
            LocalDate scoreDate,
            Double riskScore,
            Double diaryComponent,
            Double measurementComponent,
            Double sleepComponent,
            Integer measurementCount,
            Integer anomalyCount
    ) {
        public static TodayScoreResponse from(DailyRiskScore score) {
            return new TodayScoreResponse(
                    score.getId(),
                    score.getScoreDate(),
                    score.getRiskScore(),
                    score.getDiaryComponent(),
                    score.getMeasurementComponent(),
                    score.getSleepComponent(),
                    score.getMeasurementCount(),
                    score.getAnomalyCount()
            );
        }
    }
}
