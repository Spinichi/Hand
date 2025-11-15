package com.finger.hand_backend.counseling.service;

import com.finger.hand_backend.counseling.dto.CounselingAnalysisResult;
import com.finger.hand_backend.counseling.entity.CounselingReport;
import com.finger.hand_backend.counseling.repository.CounselingReportRepository;
import com.finger.hand_backend.diary.entity.DiaryConversation;
import com.finger.hand_backend.diary.repository.DiaryConversationRepository;
import com.finger.hand_backend.report.service.BiometricDataCollector;
import com.finger.hand_backend.report.service.ReportAnalysisClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 상담 분석 서비스 (관리자용)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CounselingService {

    private final CounselingReportRepository counselingReportRepository;
    private final DiaryConversationRepository diaryConversationRepository;
    private final BiometricDataCollector biometricDataCollector;
    private final ReportAnalysisClient reportAnalysisClient;

    /**
     * 관리자 상담용 분석
     *
     * @param userId    대상 사용자 ID
     * @param startDate 시작일
     * @param endDate   종료일
     * @return 상담 조언
     */
    @Transactional(readOnly = true)
    public CounselingAnalysisResult analyzeCounseling(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("Analyzing counseling for user {} ({} ~ {})", userId, startDate, endDate);

        // 1. 해당 기간의 다이어리 조회
        List<DiaryConversation> diaries = diaryConversationRepository
                .findByUserIdAndSessionDateBetweenOrderBySessionDateAsc(userId, startDate, endDate)
                .stream()
                .filter(d -> d.getEmotionAnalysis() != null) // 완료된 다이어리만
                .collect(Collectors.toList());

        log.debug("Found {} completed diaries", diaries.size());

        if (diaries.isEmpty()) {
            throw new IllegalStateException("해당 기간에 작성된 다이어리가 없습니다.");
        }

        // 2. totalSummary 생성 (관리자용 RAG: 모든 longSummary 이어붙이기)
        String totalSummary = diaries.stream()
                .map(diary -> diary.getEmotionAnalysis().getLongSummary())
                .collect(Collectors.joining(" "));

        // 3. 일별 다이어리 데이터 구성
        List<Map<String, Object>> dailyDiaries = new ArrayList<>();
        for (DiaryConversation diary : diaries) {
            Map<String, Object> dailyDiary = new HashMap<>();
            dailyDiary.put("date", diary.getSessionDate().toString());  // LocalDate -> String (ISO 8601)
            dailyDiary.put("longSummary", diary.getEmotionAnalysis().getLongSummary());
            dailyDiary.put("shortSummary", diary.getEmotionAnalysis().getShortSummary());
            dailyDiary.put("depressionScore", diary.getEmotionAnalysis().getDepressionScore());
            dailyDiaries.add(dailyDiary);
        }

        // 4. 생체 데이터 수집
        BiometricDataCollector.BiometricDataResult biometricData =
                biometricDataCollector.collectBiometricData(userId, startDate, endDate);

        // 5. FastAPI로 분석 요청 (관리자용)
        Map<String, Object> biometrics = new HashMap<>();
        biometrics.put("baseline", biometricData.getUserBaseline());
        biometrics.put("anomalies", biometricData.getAnomalies());
        biometrics.put("userInfo", biometricData.getUserInfo());

        ReportAnalysisClient.ReportAnalysisResult analysisResult =
                reportAnalysisClient.analyzeManagerAdvice(
                        userId,
                        dailyDiaries,
                        biometrics,
                        totalSummary
                );

        String counselingAdvice = analysisResult.getAdvice();

        // 6. DB에 저장
        CounselingReport savedReport = counselingReportRepository.save(
                CounselingReport.builder()
                        .userId(userId)
                        .startDate(startDate)
                        .endDate(endDate)
                        .diaryCount(diaries.size())
                        .counselingAdvice(counselingAdvice)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        log.info("Counseling report saved with id: {}", savedReport.getId());

        // 7. 결과 반환
        return CounselingAnalysisResult.builder()
                .reportId(savedReport.getId())
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .diaryCount(diaries.size())
                .counselingAdvice(counselingAdvice)
                .createdAt(savedReport.getCreatedAt())
                .build();
    }

    /**
     * 상담 보고서 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<CounselingAnalysisResult> getCounselingReports(Long userId, Pageable pageable) {
        log.info("Getting counseling reports for user: {}", userId);

        Page<CounselingReport> reports = counselingReportRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return reports.map(this::toDto);
    }

    /**
     * 상담 보고서 상세 조회
     */
    @Transactional(readOnly = true)
    public CounselingAnalysisResult getCounselingReport(Long reportId) {
        log.info("Getting counseling report: {}", reportId);

        CounselingReport report = counselingReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("상담 보고서를 찾을 수 없습니다."));

        return toDto(report);
    }

    /**
     * 최신 상담 보고서 조회
     */
    @Transactional(readOnly = true)
    public CounselingAnalysisResult getLatestCounselingReport(Long userId) {
        log.info("Getting latest counseling report for user: {}", userId);

        CounselingReport report = counselingReportRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("상담 보고서를 찾을 수 없습니다."));

        return toDto(report);
    }

    /**
     * Entity -> DTO 변환
     */
    private CounselingAnalysisResult toDto(CounselingReport report) {
        return CounselingAnalysisResult.builder()
                .reportId(report.getId())
                .userId(report.getUserId())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .diaryCount(report.getDiaryCount())
                .counselingAdvice(report.getCounselingAdvice())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
