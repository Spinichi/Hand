package com.finger.hand_backend.counseling.service;

import com.finger.hand_backend.counseling.dto.CounselingAnalysisResult;
import com.finger.hand_backend.counseling.entity.CounselingReport;
import com.finger.hand_backend.counseling.entity.CounselingReportDetail;
import com.finger.hand_backend.counseling.repository.CounselingReportDetailRepository;
import com.finger.hand_backend.counseling.repository.CounselingReportRepository;
import com.finger.hand_backend.diary.entity.DiaryConversation;
import com.finger.hand_backend.diary.repository.DiaryConversationRepository;
import com.finger.hand_backend.group.entity.GroupMember;
import com.finger.hand_backend.group.entity.GroupRole;
import com.finger.hand_backend.group.repository.GroupMemberRepository;
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
    private final CounselingReportDetailRepository counselingReportDetailRepository;
    private final DiaryConversationRepository diaryConversationRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final BiometricDataCollector biometricDataCollector;
    private final ReportAnalysisClient reportAnalysisClient;

    /**
     * 관리자가 특정 그룹에서 대상 유저를 관리할 권한이 있는지 검증
     * - 관리자가 해당 그룹에서 MANAGER 역할이어야 함
     * - 대상 유저가 해당 그룹의 멤버여야 함
     */
    private void validateManagerAccessToGroup(Long managerId, Long groupId, Long targetUserId) {
        // 1. 관리자가 해당 그룹의 MANAGER인지 확인
        GroupMember managerMember = groupMemberRepository.findByGroupIdAndUserId(groupId, managerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹에 접근 권한이 없습니다."));

        if (managerMember.getRole() != GroupRole.MANAGER) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }

        // 2. 대상 유저가 해당 그룹의 멤버인지 확인
        groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 이 그룹의 멤버가 아닙니다."));

        log.debug("Manager {} has access to user {} in group {}", managerId, targetUserId, groupId);
    }

    /**
     * 특정 그룹에서 유저의 specialNotes 조회
     */
    private String getSpecialNotes(Long groupId, Long userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .map(GroupMember::getSpecialNotes)
                .orElse("");
    }

    /**
     * 관리자 상담용 분석
     *
     * @param managerId 관리자 ID
     * @param groupId   그룹 ID
     * @param userId    대상 사용자 ID
     * @param startDate 시작일
     * @param endDate   종료일
     * @return 상담 조언
     */
    @Transactional
    public CounselingAnalysisResult analyzeCounseling(Long managerId, Long groupId, Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("Analyzing counseling for user {} by manager {} in group {} ({} ~ {})", userId, managerId, groupId, startDate, endDate);

        // 1. 관리자 권한 검증
        validateManagerAccessToGroup(managerId, groupId, userId);

        // 2. 중복 체크: 같은 기간 보고서가 이미 있으면 기존 것 반환
        Optional<CounselingReport> existingReport = counselingReportRepository
                .findTopByUserIdAndStartDateAndEndDateOrderByCreatedAtDesc(userId, startDate, endDate);

        if (existingReport.isPresent()) {
            log.info("Existing report found for user {} ({} ~ {}), returning existing report: {}",
                    userId, startDate, endDate, existingReport.get().getId());
            return getCounselingReport(managerId, groupId, existingReport.get().getId());
        }

        log.info("No existing report found, creating new counseling report");

        // 3. 해당 기간의 다이어리 조회
        // Between 쿼리에서 endDate 포함을 위해 +1일 (MongoDB LocalDate 변환 이슈 대응)
        List<DiaryConversation> diaries = diaryConversationRepository
                .findByUserIdAndSessionDateBetweenOrderBySessionDateAsc(userId, startDate.minusDays(1), endDate.plusDays(1))
                .stream()
                .filter(d -> d.getEmotionAnalysis() != null) // 완료된 다이어리만
                .filter(d -> !d.getSessionDate().isBefore(startDate) && !d.getSessionDate().isAfter(endDate)) // 정확한 범위 필터링
                .collect(Collectors.toList());

        log.debug("Found {} completed diaries", diaries.size());

        if (diaries.isEmpty()) {
            throw new IllegalStateException("해당 기간에 작성된 다이어리가 없습니다.");
        }

        // 4. totalSummary 생성 (관리자용 RAG: 모든 longSummary 이어붙이기)
        String totalSummary = diaries.stream()
                .map(diary -> diary.getEmotionAnalysis().getLongSummary())
                .collect(Collectors.joining(" "));

        // 5. 일별 다이어리 데이터 구성
        List<Map<String, Object>> dailyDiaries = new ArrayList<>();
        for (DiaryConversation diary : diaries) {
            Map<String, Object> dailyDiary = new HashMap<>();
            dailyDiary.put("date", diary.getSessionDate().toString());  // LocalDate -> String (ISO 8601)
            dailyDiary.put("longSummary", diary.getEmotionAnalysis().getLongSummary());
            dailyDiary.put("shortSummary", diary.getEmotionAnalysis().getShortSummary());
            dailyDiary.put("depressionScore", diary.getEmotionAnalysis().getDepressionScore());
            dailyDiaries.add(dailyDiary);
        }

        // 6. 생체 데이터 수집
        BiometricDataCollector.BiometricDataResult biometricData =
                biometricDataCollector.collectBiometricData(userId, startDate, endDate);

        // 7. FastAPI로 분석 요청 (관리자용)
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

        // 8. 통계 계산
        DoubleSummaryStatistics scoreStats = diaries.stream()
                .mapToDouble(d -> d.getEmotionAnalysis().getDepressionScore())
                .summaryStatistics();

        // 9. CounselingReportDetail (MongoDB) 저장
        CounselingReportDetail detail = CounselingReportDetail.builder()
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .dailyDiaries(dailyDiaries)
                .userBaseline(biometricData.getUserBaseline())
                .anomalies(biometricData.getAnomalies())
                .userInfo(biometricData.getUserInfo())
                .report(analysisResult.getReport())
                .advice(analysisResult.getAdvice())
                .totalDiaryCount(diaries.size())
                .averageDepressionScore(scoreStats.getAverage())
                .maxDepressionScore(scoreStats.getMax())
                .minDepressionScore(scoreStats.getMin())
                .createdAt(LocalDateTime.now())
                .analyzedAt(LocalDateTime.now())
                .build();

        detail = counselingReportDetailRepository.save(detail);
        log.debug("CounselingReportDetail saved with ID: {}", detail.getId());

        // 10. CounselingReport (MySQL) 메타데이터 저장
        CounselingReport savedReport = counselingReportRepository.save(
                CounselingReport.builder()
                        .userId(userId)
                        .startDate(startDate)
                        .endDate(endDate)
                        .diaryCount(diaries.size())
                        .mongodbReportId(detail.getId())
                        .counselingAdvice(analysisResult.getAdvice())  // 하위 호환성
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        log.info("Counseling report saved with id: {}", savedReport.getId());

        // 11. 결과 반환 (specialNotes는 toDto에서 실시간 조회)
        return toDto(savedReport, detail, groupId);
    }

    /**
     * 상담 보고서 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<CounselingAnalysisResult> getCounselingReports(Long managerId, Long groupId, Long userId, Pageable pageable) {
        log.info("Getting counseling reports for user {} by manager {} in group {}", userId, managerId, groupId);

        // 관리자 권한 검증
        validateManagerAccessToGroup(managerId, groupId, userId);

        Page<CounselingReport> reports = counselingReportRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return reports.map(report -> {
            // MongoDB에서 상세 데이터 조회
            CounselingReportDetail detail = counselingReportDetailRepository.findById(report.getMongodbReportId())
                    .orElse(null);

            if (detail != null) {
                return toDto(report, detail, groupId);
            } else {
                // MongoDB 데이터가 없는 경우 MySQL만 사용
                return toDto(report, groupId);
            }
        });
    }

    /**
     * 상담 보고서 상세 조회
     */
    @Transactional(readOnly = true)
    public CounselingAnalysisResult getCounselingReport(Long managerId, Long groupId, Long reportId) {
        log.info("Getting counseling report {} by manager {} in group {}", reportId, managerId, groupId);

        CounselingReport report = counselingReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("상담 보고서를 찾을 수 없습니다."));

        // 관리자 권한 검증
        validateManagerAccessToGroup(managerId, groupId, report.getUserId());

        // MongoDB에서 상세 데이터 조회
        CounselingReportDetail detail = counselingReportDetailRepository.findById(report.getMongodbReportId())
                .orElseThrow(() -> new IllegalStateException("상담 보고서 상세 정보를 찾을 수 없습니다."));

        return toDto(report, detail, groupId);
    }

    /**
     * 최신 상담 보고서 조회
     */
    @Transactional(readOnly = true)
    public CounselingAnalysisResult getLatestCounselingReport(Long managerId, Long groupId, Long userId) {
        log.info("Getting latest counseling report for user {} by manager {} in group {}", userId, managerId, groupId);

        // 관리자 권한 검증
        validateManagerAccessToGroup(managerId, groupId, userId);

        CounselingReport report = counselingReportRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("상담 보고서를 찾을 수 없습니다."));

        // MongoDB에서 상세 데이터 조회
        CounselingReportDetail detail = counselingReportDetailRepository.findById(report.getMongodbReportId())
                .orElseThrow(() -> new IllegalStateException("상담 보고서 상세 정보를 찾을 수 없습니다."));

        return toDto(report, detail, groupId);
    }

    /**
     * Entity -> DTO 변환 (목록 조회용 - 상세 데이터 없음)
     */
    private CounselingAnalysisResult toDto(CounselingReport report, Long groupId) {
        // specialNotes는 GroupMember에서 실시간 조회 (관리자와 공통 그룹에서)
        String specialNotes = getSpecialNotes(groupId, report.getUserId());

        return CounselingAnalysisResult.builder()
                .reportId(report.getId())
                .userId(report.getUserId())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .diaryCount(report.getDiaryCount())
                .advice(report.getCounselingAdvice())  // 하위 호환성
                .specialNotes(specialNotes)  // 실시간 조회한 값
                .createdAt(report.getCreatedAt())
                .build();
    }

    /**
     * Entity -> DTO 변환 (상세 조회용 - MongoDB 데이터 포함)
     */
    private CounselingAnalysisResult toDto(CounselingReport report, CounselingReportDetail detail, Long groupId) {
        // specialNotes는 GroupMember에서 실시간 조회 (관리자와 공통 그룹에서)
        String specialNotes = getSpecialNotes(groupId, report.getUserId());

        return CounselingAnalysisResult.builder()
                .reportId(report.getId())
                .userId(report.getUserId())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .diaryCount(report.getDiaryCount())
                .dailyDiaries(detail.getDailyDiaries())
                .report(detail.getReport())
                .advice(detail.getAdvice())
                .averageDepressionScore(detail.getAverageDepressionScore())
                .maxDepressionScore(detail.getMaxDepressionScore())
                .minDepressionScore(detail.getMinDepressionScore())
                .specialNotes(specialNotes)  // 실시간 조회한 값
                .createdAt(report.getCreatedAt())
                .build();
    }
}
