package com.finger.hand_backend.report.service;

import com.finger.hand_backend.diary.entity.DiaryConversation;
import com.finger.hand_backend.diary.repository.DiaryConversationRepository;
import com.finger.hand_backend.report.entity.ReportStatus;
import com.finger.hand_backend.report.entity.WeeklyReport;
import com.finger.hand_backend.report.entity.WeeklyReportDetail;
import com.finger.hand_backend.report.repository.WeeklyReportDetailRepository;
import com.finger.hand_backend.report.repository.WeeklyReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 주간 보고서 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportService {

    private final DiaryConversationRepository diaryConversationRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final WeeklyReportDetailRepository weeklyReportDetailRepository;
    private final BiometricDataCollector biometricDataCollector;
    private final ReportAnalysisClient reportAnalysisClient;

    /**
     * 주간 보고서 생성
     *
     * @param userId 사용자 ID
     * @param date   기준 날짜 (해당 주의 아무 날짜)
     * @return 보고서 생성 결과
     */
    @Transactional
    public ReportGenerationResult generateWeeklyReport(Long userId, LocalDate date) {
        log.info("Generating weekly report for user {} on {}", userId, date);

        // 1. ISO 8601 주차 계산
        WeekFields weekFields = WeekFields.ISO;
        int year = date.get(weekFields.weekBasedYear());
        int weekNumber = date.get(weekFields.weekOfWeekBasedYear());

        log.debug("Week: {}-W{}", year, weekNumber);

        // 2. 이미 생성된 보고서 확인
        Optional<WeeklyReport> existingReport = weeklyReportRepository
                .findByUserIdAndYearAndWeekNumber(userId, year, weekNumber);

        if (existingReport.isPresent()) {
            log.warn("Weekly report already exists for user {} {}-W{}", userId, year, weekNumber);
            return ReportGenerationResult.existing(existingReport.get().getId());
        }

        // 3. 주의 시작일(월요일)과 종료일(일요일) 계산
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        log.debug("Week range: {} ~ {}", weekStart, weekEnd);

        // 4. 다이어리 조회 (COMPLETED 상태만)
        // Between 쿼리에서 endDate 포함을 위해 +1일 (MongoDB LocalDate 변환 이슈 대응)
        List<DiaryConversation> diaries = diaryConversationRepository
                .findByUserIdAndSessionDateBetweenOrderBySessionDateAsc(userId, weekStart.minusDays(1), weekEnd.plusDays(1))
                .stream()
                .filter(d -> d.getEmotionAnalysis() != null) // 완료된 다이어리만 (emotionAnalysis 존재)
                .filter(d -> !d.getSessionDate().isBefore(weekStart) && !d.getSessionDate().isAfter(weekEnd)) // 정확한 범위 필터링
                .collect(Collectors.toList());

        log.debug("Found {} completed diaries", diaries.size());

        // 5. 최소 2개 이상 확인
        if (diaries.size() < 2) {
            throw new IllegalStateException(
                    String.format("주간 보고서 생성을 위해서는 최소 2개의 다이어리가 필요합니다. (현재: %d개)", diaries.size()));
        }

        // 6. 일별 다이어리 데이터 구성
        List<Map<String, Object>> dailyDiaries = new ArrayList<>();

        for (DiaryConversation diary : diaries) {
            Map<String, Object> dailyDiary = new HashMap<>();
            dailyDiary.put("date", diary.getSessionDate().toString());  // LocalDate -> String (ISO 8601)
            dailyDiary.put("longSummary", diary.getEmotionAnalysis().getLongSummary());
            dailyDiary.put("shortSummary", diary.getEmotionAnalysis().getShortSummary());
            dailyDiary.put("depressionScore", diary.getEmotionAnalysis().getDepressionScore());
            dailyDiaries.add(dailyDiary);
        }

        // 7. 생체 데이터 수집
        BiometricDataCollector.BiometricDataResult biometricData =
                biometricDataCollector.collectBiometricData(userId, weekStart, weekEnd);

        // 8. FastAPI로 분석 요청 (개인용)
        Map<String, Object> biometricsForApi = new HashMap<>();
        biometricsForApi.put("baseline", biometricData.getUserBaseline());
        biometricsForApi.put("anomalies", biometricData.getAnomalies());
        biometricsForApi.put("userInfo", biometricData.getUserInfo());  // height, weight, residenceType 포함

        // 9. AI 분석용 user_info 생성 (height, weight 제외, family로 변경)
        Map<String, Object> userInfoForAi = new HashMap<>();
        Map<String, Object> baseUserInfo = biometricData.getUserInfo();
        userInfoForAi.put("age", baseUserInfo.get("age"));
        userInfoForAi.put("gender", baseUserInfo.get("gender"));
        userInfoForAi.put("job", baseUserInfo.get("job"));
        userInfoForAi.put("disease", baseUserInfo.get("disease"));
        userInfoForAi.put("family", baseUserInfo.get("residenceType"));  // residenceType → family

        // 개인용 보고서는 totalSummary를 빈 문자열로 전달 (관리자용만 RAG 사용)
        ReportAnalysisClient.ReportAnalysisResult analysisResult =
                reportAnalysisClient.analyzeIndividualReport(
                        userId,
                        dailyDiaries,
                        biometricsForApi,
                        "",  // 개인용은 빈 문자열
                        userInfoForAi  // AI 분석용 사용자 정보
                );

        // 10. 통계 계산
        DoubleSummaryStatistics scoreStats = diaries.stream()
                .mapToDouble(d -> d.getEmotionAnalysis().getDepressionScore())
                .summaryStatistics();

        // 11. WeeklyReportDetail (MongoDB) 저장
        WeeklyReportDetail detail = WeeklyReportDetail.builder()
                .userId(userId)
                .year(year)
                .weekNumber(weekNumber)
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .dailyDiaries(dailyDiaries)
                .userBaseline(biometricData.getUserBaseline())
                .anomalies(biometricData.getAnomalies())
                .userInfo(biometricData.getUserInfo())
                .report(analysisResult.getReport())
                .emotionalAdvice(analysisResult.getAdvice())  // FastAPI 스펙: advice 필드 사용
                .totalDiaryCount(diaries.size())
                .averageDepressionScore(scoreStats.getAverage())
                .maxDepressionScore(scoreStats.getMax())
                .minDepressionScore(scoreStats.getMin())
                .createdAt(LocalDateTime.now())
                .analyzedAt(LocalDateTime.now())
                .build();

        detail = weeklyReportDetailRepository.save(detail);
        log.debug("WeeklyReportDetail saved with ID: {}", detail.getId());

        // 12. WeeklyReport (MySQL) 메타데이터 저장
        WeeklyReport report = WeeklyReport.builder()
                .userId(userId)
                .year(year)
                .weekNumber(weekNumber)
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .mongodbReportId(detail.getId())
                .diaryCount(diaries.size())
                .status(ReportStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        report = weeklyReportRepository.save(report);
        log.info("Weekly report created successfully with ID: {}", report.getId());

        return ReportGenerationResult.created(report.getId());
    }

    /**
     * 주간 보고서 조회
     *
     * @param userId 사용자 ID
     * @param reportId 보고서 ID
     * @return 보고서 상세
     */
    @Transactional(readOnly = true)
    public WeeklyReportDetail getWeeklyReportDetail(Long userId, Long reportId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("보고서를 찾을 수 없습니다."));

        if (!report.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        return weeklyReportDetailRepository.findById(report.getMongodbReportId())
                .orElseThrow(() -> new IllegalStateException("보고서 상세 정보를 찾을 수 없습니다."));
    }

    /**
     * 최신 주간 보고서 조회
     */
    @Transactional(readOnly = true)
    public Optional<WeeklyReportDetail> getLatestWeeklyReport(Long userId) {
        return weeklyReportRepository.findTopByUserIdOrderByYearDescWeekNumberDesc(userId)
                .flatMap(report -> weeklyReportDetailRepository.findById(report.getMongodbReportId()));
    }

    /**
     * 주간 보고서 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<WeeklyReport> getWeeklyReports(Long userId, Pageable pageable) {
        return weeklyReportRepository.findByUserIdOrderByYearDescWeekNumberDesc(userId, pageable);
    }
}
