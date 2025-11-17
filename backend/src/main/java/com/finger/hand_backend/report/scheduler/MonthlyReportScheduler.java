package com.finger.hand_backend.report.scheduler;

import com.finger.hand_backend.diary.entity.DiaryConversation;
import com.finger.hand_backend.diary.repository.DiaryConversationRepository;
import com.finger.hand_backend.report.service.MonthlyReportService;
import com.finger.hand_backend.report.service.ReportGenerationResult;
import com.finger.hand_backend.user.entity.IndividualUser;
import com.finger.hand_backend.user.repository.IndividualUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * 월간 보고서 자동 생성 스케줄러
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyReportScheduler {

    private final IndividualUserRepository individualUserRepository;
    private final DiaryConversationRepository diaryConversationRepository;
    private final MonthlyReportService monthlyReportService;

    /**
     * 매월 1일 새벽 3시, 지난 달 보고서 자동 생성
     * - 최소 2개 다이어리 필요
     */
    @Scheduled(cron = "0 0 3 1 * *", zone = "Asia/Seoul") // 매월 1일 새벽 3시
    public void generateMonthlyReports() {
        log.info("=== Monthly Report Auto Generation Started ===");

        // 1. 지난 달 계산
        LocalDate today = LocalDate.now();
        YearMonth lastMonth = YearMonth.from(today.minusMonths(1));
        LocalDate lastMonthStart = lastMonth.atDay(1);  // 지난 달 1일
        LocalDate lastMonthEnd = lastMonth.atEndOfMonth();  // 지난 달 마지막 날

        log.info("Target month: {} ~ {}", lastMonthStart, lastMonthEnd);

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
                // 3-1. 지난 달 완료된 다이어리 개수 확인
                List<DiaryConversation> diaries = diaryConversationRepository
                        .findByUserIdAndSessionDateBetweenOrderBySessionDateAsc(
                                userId,
                                lastMonthStart.minusDays(1),
                                lastMonthEnd.plusDays(1)
                        )
                        .stream()
                        .filter(d -> d.getEmotionAnalysis() != null)  // 완료된 다이어리만
                        .filter(d -> !d.getSessionDate().isBefore(lastMonthStart) && !d.getSessionDate().isAfter(lastMonthEnd))
                        .toList();

                // 3-2. 최소 2개 미만이면 스킵
                if (diaries.size() < 2) {
                    log.debug("User {} skipped: insufficient diaries ({} < 2)", userId, diaries.size());
                    skippedCount++;
                    continue;
                }

                // 3-3. 보고서 생성 시도
                ReportGenerationResult result = monthlyReportService.generateMonthlyReport(
                        userId,
                        lastMonthStart  // 지난 달 1일 날짜
                );

                if (result.isNewlyCreated()) {
                    generatedCount++;
                    log.info("Monthly report generated for user {}: reportId={}", userId, result.getReportId());
                } else {
                    // 이미 존재하는 경우
                    skippedCount++;
                    log.debug("User {} skipped: report already exists (reportId={})", userId, result.getReportId());
                }

            } catch (Exception e) {
                errorCount++;
                log.error("Failed to generate monthly report for user {}", userId, e);
            }
        }

        log.info("=== Monthly Report Auto Generation Completed: {} generated, {} skipped, {} errors ===",
                generatedCount, skippedCount, errorCount);
    }
}
