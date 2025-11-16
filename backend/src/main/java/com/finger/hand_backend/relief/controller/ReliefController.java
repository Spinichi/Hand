// ReliefController.java
package com.finger.hand_backend.relief.controller;

import com.finger.hand_backend.common.dto.ApiResponse;
import com.finger.hand_backend.relief.service.ReliefQueryService;
import com.finger.hand_backend.relief.service.ReliefService;
import com.finger.hand_backend.relief.entity.TriggerType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/relief")
@RequiredArgsConstructor
public class ReliefController {

    private final ReliefService reliefService;
    private final ReliefQueryService queryService;

    // 세션 시작 (A모드: beforeStress 직접 주입 가능)
    @PostMapping("/sessions/start")
    public ResponseEntity<?> start(Authentication auth, @RequestBody StartReq req) {
        Long userId = Long.valueOf(auth.getName());
        var cmd = new ReliefService.StartCommand(
                req.getInterventionId(),        // Long
                req.getTriggerType(),           // TriggerType
                req.getAnomalyDetectionId(),    // Long
                req.getGestureCode(),           // String
                req.getStartedAt()              // LocalDateTime
        );
        var log = reliefService.start(userId, cmd);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        new StartRes(log.getId(), log.getBeforeStress(), log.getStartedAt()),
                        "session started"));
    }

    // 세션 종료 (A모드: afterStress/userRating 전달)
    @PostMapping("/sessions/{id}/end")
    public ResponseEntity<?> end(Authentication auth, @PathVariable Long id, @RequestBody EndReq req) {
        Long userId = Long.valueOf(auth.getName());
        var cmd = new ReliefService.EndCommand(req.getEndedAt(), req.getUserRating());
        var log = reliefService.end(userId, id, cmd);
        return ResponseEntity.ok(ApiResponse.success(
                new EndRes(log.getAfterStress(), log.getDurationSeconds(), log.getEndedAt()),
                "session ended"));
    }

    @GetMapping("/stats/my")
    public ResponseEntity<?> myStats(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(queryService.getMyStats(userId), "my stats"));
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> mySessions(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(
                // 간단 목록; 필요시 DTO 변환
                queryService.getMyStats(userId), "my sessions"));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getReliefHistory(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(
                queryService.getReliefHistory(userId, page, size), "relief history"));
    }

    // ===== DTOs =====
    @Data
    public static class StartReq {
        @NotNull private Long interventionId;
        @NotNull private TriggerType triggerType; // AUTO_SUGGEST or MANUAL
        private Long anomalyDetectionId;
        private String gestureCode;
        private LocalDateTime startedAt;    // 없으면 서버가 now()
    }
    public record StartRes(Long sessionId, Integer beforeStress, LocalDateTime startedAt) {}

    @Data
    public static class EndReq {
        private LocalDateTime endedAt; // 없으면 서버가 now()
        private Integer userRating;    // 1~5 선택
    }
    public record EndRes(Integer afterStress, Integer durationSeconds, LocalDateTime endedAt) {}
}
