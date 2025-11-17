package com.finger.hand_backend.report.scheduler;

import com.finger.hand_backend.diary.entity.DiaryConversation;
import com.finger.hand_backend.diary.repository.DiaryConversationRepository;
import com.finger.hand_backend.report.service.ReportGenerationResult;
import com.finger.hand_backend.report.service.WeeklyReportService;
import com.finger.hand_backend.user.entity.IndividualUser;
import com.finger.hand_backend.user.repository.IndividualUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * 주간 보고서 자동 생성 스케줄러
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportScheduler {

    private final IndividualUserRepository individualUserRepository;
    private final DiaryConversationRepository diaryConversationRepository;
    private final WeeklyReportService weeklyReportService;

    /**
     * 매주 월요일 새벽 2시, 지난 주 보고서 자동 생성
     * - ISO 8601 주차 기준 (월~일)
     * - 최소 2개 다이어리 필요
     */
    @Scheduled(cron = "0 0 2 * * MON", zone = "Asia/Seoul") // 매주 월요일 새벽 2시
    public void generateWeeklyReports() {
        log.info("=== Weekly Report Auto Generation Started ===");

        // 1. 지난 주 계산 (월요일 기준 -7일 ~ -1일)
        LocalDate today = LocalDate.now();
        LocalDate lastWeekStart = today.minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastWeekEnd = lastWeekStart.plusDays(6); // 일요일

        log.info("Target week: {} ~ {}", lastWeekStart, lastWeekEnd);

        // 2. 모든 개인 사용자 조회
        List<IndividualUser> allUsers = individualUserRepository.findAll();
        log.info("Total users: {}", allUsers.size());

        int generatedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;

        // 3. 각 사용자별 보고서 생성
        for (IndividualUser user : allUsers) {
            Long userId = user.getUserId();

            try {
                // 3-1. 지난 주 완료된 다이어리 개수 확인
                List<DiaryConversation> diaries = diaryConversationRepository
                        .findByUserIdAndSessionDateBetweenOrderBySessionDateAsc(
                                userId,
                                lastWeekStart.minusDays(1),
                                lastWeekEnd.plusDays(1)
                        )
                        .stream()
                        .filter(d -> d.getEmotionAnalysis() != null)  // 완료된 다이어리만
                        .filter(d -> !d.getSessionDate().isBefore(lastWeekStart) && !d.getSessionDate().isAfter(lastWeekEnd))
                        .toList();

                // 3-2. 최소 2개 미만이면 스킵
                if (diaries.size() < 2) {
                    log.debug("User {} skipped: insufficient diaries ({} < 2)", userId, diaries.size());
                    skippedCount++;
                    continue;
                }

                // 3-3. 보고서 생성 시도
                ReportGenerationResult result = weeklyReportService.generateWeeklyReport(
                        userId,
                        lastWeekStart  // 지난 주 월요일 날짜
                );

                if (result.isNewlyCreated()) {
                    generatedCount++;
                    log.info("Weekly report generated for user {}: reportId={}", userId, result.getReportId());
                } else {
                    // 이미 존재하는 경우
                    skippedCount++;
                    log.debug("User {} skipped: report already exists (reportId={})", userId, result.getReportId());
                }

            } catch (Exception e) {
                errorCount++;
                log.error("Failed to generate weekly report for user {}", userId, e);
            }
        }

        log.info("=== Weekly Report Auto Generation Completed: {} generated, {} skipped, {} errors ===",
                generatedCount, skippedCount, errorCount);
    }
}
