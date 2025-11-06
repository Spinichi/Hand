package com.finger.hand_backend.relief.service;

import com.finger.hand_backend.measurement.Measurement;
import com.finger.hand_backend.measurement.MeasurementRepository;
import com.finger.hand_backend.relief.entity.InterventionLog;
import com.finger.hand_backend.relief.entity.TriggerType;
import com.finger.hand_backend.relief.repository.InterventionLogRepository;
import com.finger.hand_backend.relief.repository.InterventionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReliefService {

    private final InterventionRepository interventionRepo;
    private final InterventionLogRepository logRepo;
    private final MeasurementRepository measurementRepo;

    @Value("${relief.match.post-window-minutes:5}")
    private int postWindowMinutes;

    @Transactional
    public InterventionLog start(Long userId, StartCommand cmd) {
        if (!interventionRepo.existsById(cmd.interventionId()))
            throw new IllegalArgumentException("intervention not found");

        LocalDateTime startedAt = cmd.startedAt() != null ? cmd.startedAt() : LocalDateTime.now();

        Integer beforeStress = resolveBeforeStress(userId, startedAt);

        InterventionLog log = InterventionLog.builder()
                .userId(userId)
                .interventionId(cmd.interventionId())
                .anomalyDetectionId(cmd.anomalyDetectionId())
                .triggerType(cmd.triggerType())
                .startedAt(startedAt)
                .gestureCode(cmd.gestureCode())
                .beforeStress(beforeStress)   // ← 여기서 자동 주입
                .createdAt(LocalDateTime.now())
                .build();

        return logRepo.save(log);
    }

    @Transactional
    public InterventionLog end(Long userId, Long logId, EndCommand cmd) {
        InterventionLog log = logRepo.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("session not found"));
        if (!log.getUserId().equals(userId)) throw new IllegalArgumentException("forbidden");

        LocalDateTime endedAt = cmd.endedAt() != null ? cmd.endedAt() : LocalDateTime.now();
        log.setEndedAt(endedAt);
        log.setDurationSeconds(calcDuration(log.getStartedAt(), endedAt));

        if (log.getAfterStress() == null) {
            resolveAfterStress(userId, endedAt).ifPresent(log::setAfterStress);
        }
        if (cmd.userRating() != null) log.setUserRating(cmd.userRating());
        return log;
    }

    // -------- 매칭 핵심 --------
    private Integer resolveBeforeStress(Long userId, LocalDateTime startedAt) {
        // 1차: startedAt <= 가장 최근 1건
        var one = measurementRepo
                .findLatestAtOrBefore(userId, startedAt, PageRequest.of(0, 1))
                .stream().findFirst();
        if (one.isPresent()) return one.get().getStressIndex();

        // 2차: 경계/지터 백업(시작 시각 + 1초까지 허용)
        var backup = measurementRepo
                .findLatestAtOrBefore(userId, startedAt.plusSeconds(1), PageRequest.of(0, 1))
                .stream().findFirst();
        return backup.map(Measurement::getStressIndex).orElse(null);
    }

    private Optional<Integer> resolveAfterStress(Long userId, LocalDateTime endedAt) {
        LocalDateTime deadline = endedAt.plusMinutes(postWindowMinutes);
        return measurementRepo
                .findFirstBetween(userId, endedAt, deadline, PageRequest.of(0, 1))
                .stream().findFirst()
                .map(Measurement::getStressIndex);
    }

    private Integer calcDuration(LocalDateTime s, LocalDateTime e) {
        if (s == null || e == null) return null;
        long sec = java.time.Duration.between(s, e).getSeconds();
        return (int) Math.max(0, Math.min(sec, Integer.MAX_VALUE));
    }

    // 커맨드
    public record StartCommand(Long interventionId, TriggerType triggerType,
                               Long anomalyDetectionId, String gestureCode, LocalDateTime startedAt) {}
    public record EndCommand(LocalDateTime endedAt, Integer userRating) {}
}
