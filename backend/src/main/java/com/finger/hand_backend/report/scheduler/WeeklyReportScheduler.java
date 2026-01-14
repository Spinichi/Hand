package com.finger.hand_backend.report.scheduler;

import com.finger.hand_backend.diary.entity.DiaryConversation;
import com.finger.hand_backend.diary.repository.DiaryConversationRepository;
import com.finger.hand_backend.mockAPI.BatchUserResult;
import com.finger.hand_backend.report.service.WeeklyReportAsyncService;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 주간 보고서 자동 생성 스케줄러
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportScheduler {

    private final IndividualUserRepository individualUserRepository;
    private final DiaryConversationRepository diaryConversationRepository;
    private final WeeklyReportService weeklyReportService;           // sync용
    private final WeeklyReportAsyncService weeklyReportAsyncService; // async용

    public void runNowForTest(Long batchKey) {
        runAsync(batchKey); // 기본은 async
    }

    private List<IndividualUser> loadTargetUsers(Long batchKey) {
        if (batchKey == null) return individualUserRepository.findAll();
        return individualUserRepository.findByNameStartingWith("SeedUser_" + batchKey + "_");
    }

    /** ✅ Before: 기존 방식(순차) */
    public void runSync(Long batchKey) {
        WeekRange range = lastWeekRange();
        LocalDate lastWeekStart = range.start();
        LocalDate lastWeekEnd = range.end();

        log.info("=== Weekly Report SYNC Started === batchKey={}", batchKey);
        log.info("Target week: {} ~ {}", lastWeekStart, lastWeekEnd);

        List<IndividualUser> allUsers = loadTargetUsers(batchKey);
        log.info("Total users: {}", allUsers.size());

        long startMs = System.currentTimeMillis();

        int successCount = 0;
        int errorCount = 0;
        int skippedCount = 0; // 전원 처리라 보통 0, (exists를 스킵으로 칠 거면 여기서 증가)

        for (IndividualUser user : allUsers) {
            Long userId = user.getUserId();

            try {
                // ✅ 테스트 목적: 다이어리 개수 체크/조회 생략 (Async와 조건 동일)
                weeklyReportService.generateWeeklyReport(userId, lastWeekStart);
                successCount++;

            } catch (Exception e) {
                errorCount++;
                log.error("SYNC failed userId={}, error={}", userId, e.getClass().getSimpleName(), e);
            }
        }

        long elapsed = System.currentTimeMillis() - startMs;
        log.info("=== Weekly Report SYNC Completed: success={}, skipped={}, errors={}, elapsedMs={} ===",
                successCount, skippedCount, errorCount, elapsed);
    }



    /** ✅ After: 병렬 방식 */
    public void runAsync(Long batchKey) {
        WeekRange range = lastWeekRange();
        LocalDate lastWeekStart = range.start();
        LocalDate lastWeekEnd = range.end();

        log.info("=== Weekly Report ASYNC Started === batchKey={}", batchKey);
        log.info("Target week: {} ~ {}", lastWeekStart, lastWeekEnd);

        List<IndividualUser> allUsers = loadTargetUsers(batchKey);
        log.info("Total users: {}", allUsers.size());

        long startMs = System.currentTimeMillis();

        // ✅ 테스트 목적: seed로 다이어리 3개씩 넣었으니 전원 처리(불필요한 Mongo 조회 제거)
        List<Long> eligibleUserIds = allUsers.stream()
                .map(IndividualUser::getUserId)
                .toList();

        int skippedCount = 0;

        final int chunkSize = 50;
        int total = eligibleUserIds.size();
        int chunks = (total + chunkSize - 1) / chunkSize;

        long successCount = 0;
        long errorCount = 0;

        for (int i = 0; i < total; i += chunkSize) {
            int to = Math.min(i + chunkSize, total);
            int chunkIndex = (i / chunkSize) + 1;

            List<Long> chunk = eligibleUserIds.subList(i, to);
            log.info("ASYNC chunk {}/{} start: size={}", chunkIndex, chunks, chunk.size());

            List<CompletableFuture<BatchUserResult>> futures = chunk.stream()
                    .map(userId -> weeklyReportAsyncService.generateWeeklyReportAsync(userId, lastWeekStart)
                            .orTimeout(120, TimeUnit.SECONDS)
                            .exceptionally(ex -> BatchUserResult.fail(userId, ex.getClass().getSimpleName()))
                    )
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            long chunkSuccess = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(BatchUserResult::isSuccess)
                    .count();

            long chunkFail = futures.size() - chunkSuccess;

            successCount += chunkSuccess;
            errorCount += chunkFail;

            log.info("ASYNC chunk {}/{} end: success={}, fail={}", chunkIndex, chunks, chunkSuccess, chunkFail);
        }

        long elapsed = System.currentTimeMillis() - startMs;
        log.info("=== Weekly Report ASYNC Completed: success={}, skipped={}, errors={}, elapsedMs={} ===",
                successCount, skippedCount, errorCount, elapsed);
    }


    /** 운영 스케줄은 async만 */
    @Scheduled(cron = "0 0 2 * * MON", zone = "Asia/Seoul")
    public void generateWeeklyReports() {
        runAsync(null);
    }

    private WeekRange lastWeekRange() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);
        return new WeekRange(start, end);
    }

    private record WeekRange(LocalDate start, LocalDate end) {}
}

