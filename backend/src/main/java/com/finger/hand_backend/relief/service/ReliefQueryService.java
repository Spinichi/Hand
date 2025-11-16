// ReliefQueryService.java
package com.finger.hand_backend.relief.service;

import com.finger.hand_backend.relief.entity.Intervention;
import com.finger.hand_backend.relief.entity.InterventionLog;
import com.finger.hand_backend.relief.repository.InterventionLogRepository;
import com.finger.hand_backend.relief.repository.InterventionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReliefQueryService {

    private final InterventionLogRepository logRepo;
    private final InterventionRepository interventionRepo;

    public List<CopingStatDto> getMyStats(Long userId) {
        List<Object[]> rows = logRepo.statsByUser(userId);
        List<CopingStatDto> out = new ArrayList<>();
        for (Object[] r : rows) {
            Long interventionId = (Long) r[0];
            Long sessions = (Long) r[1];
            Double avgReduction = (Double) r[2];
            Intervention itv = interventionRepo.findById(interventionId).orElse(null);
            out.add(new CopingStatDto(
                    interventionId,
                    itv != null ? itv.getName() : ("#" + interventionId),
                    sessions.intValue(),
                    avgReduction != null ? avgReduction : 0.0
            ));
        }
        return out;
    }

    /**
     * 마음 완화 기록 화면용 API
     * - 통계: 많이 실행한 완화법 1개, 효과 좋은 완화법 1개
     * - 히스토리: 날짜별 실행 기록 (페이징)
     *
     * @param userId 사용자 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기 (날짜 개수)
     */
    public ReliefHistoryResponse getReliefHistory(Long userId, int page, int size) {
        // 1. 통계 데이터 조회
        List<Object[]> statsRows = logRepo.getStatisticsWithDays(userId);
        List<InterventionStatDto> allStats = new ArrayList<>();

        for (Object[] row : statsRows) {
            Long interventionId = ((Number) row[0]).longValue();
            Integer days = ((Number) row[1]).intValue();
            Integer sessions = ((Number) row[2]).intValue();
            Double avgReduction = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;

            Intervention intervention = interventionRepo.findById(interventionId).orElse(null);
            String name = intervention != null ? intervention.getName() : ("#" + interventionId);

            allStats.add(new InterventionStatDto(interventionId, name, days, sessions, avgReduction));
        }

        // 2. 많이 실행한 완화법 1개 (sessions 내림차순)
        InterventionStatDto mostUsed = allStats.stream()
                .max(Comparator.comparing(InterventionStatDto::sessions))
                .orElse(null);

        // 3. 효과 좋은 완화법 1개 (avgReduction 내림차순)
        InterventionStatDto mostEffective = allStats.stream()
                .max(Comparator.comparing(InterventionStatDto::avgReduction))
                .orElse(null);

        // 4. 히스토리 데이터 조회 (날짜 내림차순)
        List<InterventionLog> logs = logRepo.findByUserIdAndAfterStressIsNotNullOrderByStartedAtDesc(userId);

        // 5. 날짜별로 그룹화 (날짜 내림차순 유지)
        Map<LocalDate, List<SessionDto>> historyMap = new LinkedHashMap<>();
        for (InterventionLog log : logs) {
            LocalDate date = log.getStartedAt().toLocalDate();
            Intervention intervention = interventionRepo.findById(log.getInterventionId()).orElse(null);
            String name = intervention != null ? intervention.getName() : ("#" + log.getInterventionId());

            SessionDto session = new SessionDto(
                    log.getId(),
                    log.getInterventionId(),
                    name,
                    log.getStartedAt(),
                    log.getBeforeStress(),
                    log.getAfterStress(),
                    log.getBeforeStress() - log.getAfterStress()
            );

            historyMap.computeIfAbsent(date, k -> new ArrayList<>()).add(session);
        }

        // 6. DailyHistoryDto 리스트로 변환
        List<DailyHistoryDto> allHistory = historyMap.entrySet().stream()
                .map(entry -> new DailyHistoryDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // 7. 페이징 처리 (날짜 기준)
        int start = page * size;
        int end = Math.min(start + size, allHistory.size());
        List<DailyHistoryDto> pagedHistory = (start < allHistory.size())
                ? allHistory.subList(start, end)
                : new ArrayList<>();

        // 8. 통계와 히스토리 결합
        StatisticsDto statistics = new StatisticsDto(mostUsed, mostEffective);
        return new ReliefHistoryResponse(statistics, pagedHistory, allHistory.size(), page, size);
    }

    // ===== DTOs =====
    public record CopingStatDto(Long interventionId, String name, Integer sessions, Double avgReduction) {}

    public record InterventionStatDto(
            Long interventionId,
            String name,
            Integer days,
            Integer sessions,
            Double avgReduction
    ) {}

    public record SessionDto(
            Long sessionId,
            Long interventionId,
            String interventionName,
            LocalDateTime startedAt,
            Integer beforeStress,
            Integer afterStress,
            Integer reduction
    ) {}

    public record DailyHistoryDto(
            LocalDate date,
            List<SessionDto> sessions
    ) {}

    public record StatisticsDto(
            InterventionStatDto mostUsed,
            InterventionStatDto mostEffective
    ) {}

    public record ReliefHistoryResponse(
            StatisticsDto statistics,
            List<DailyHistoryDto> history,
            int totalElements,  // 전체 날짜 수
            int currentPage,
            int pageSize
    ) {}
}

