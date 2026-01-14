package com.finger.hand_backend.report.service;

import com.finger.hand_backend.mockAPI.BatchUserResult;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class WeeklyReportAsyncService {

    private final WeeklyReportService weeklyReportService;

    @Async("reportGenerationExecutor")
    public CompletableFuture<BatchUserResult> generateWeeklyReportAsync(Long userId, LocalDate weekStart) {
        try {
            weeklyReportService.generateWeeklyReport(userId, weekStart); // 기존 동기 로직 실행
            return CompletableFuture.completedFuture(BatchUserResult.success(userId));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                    BatchUserResult.fail(userId, e.getClass().getSimpleName())
            );
        }
    }
}

