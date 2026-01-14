package com.finger.hand_backend.diary.service;

import com.finger.hand_backend.diary.dto.DiaryCompleteResponse;
import com.finger.hand_backend.diary.dto.EmotionScores;
import com.finger.hand_backend.diary.entity.DiaryConversation;
import com.finger.hand_backend.diary.entity.DiarySession;
import com.finger.hand_backend.diary.entity.EmotionAnalysis;
import com.finger.hand_backend.diary.repository.DiaryConversationRepository;
import com.finger.hand_backend.diary.repository.DiarySessionRepository;
import com.finger.hand_backend.risk.DailyRiskScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 다이어리 Phase 3 서비스
 * - 트랜잭션 분리를 위해 별도 서비스로 분리
 * - Self-invocation 문제 해결
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryPhase3Service {

    private final DiarySessionRepository sessionRepository;
    private final DiaryConversationRepository conversationRepository;
    private final DailyRiskScoreService riskScoreService;

    /**
     * Phase 3: 결과 저장 및 완료 (트랜잭션)
     */
    @Transactional
    public DiaryCompleteResponse saveResultAndComplete(
            Long userId,
            Long sessionId,
            DiaryConversation conversation,
            EmotionAnalysis analysis
    ) {
        // 1. 세션 재조회 (영속성 컨텍스트에 포함시키기 위해)
        DiarySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다"));

        boolean isTxActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("🟢 Phase 3 - sessionId: {}, TX Active: {}", sessionId, isTxActive);

        // 2. 감정 분석 결과를 MongoDB에 저장
        conversation.setEmotionAnalysisResult(analysis);
        conversationRepository.save(conversation);

        // 3. 세션 완료
        session.complete();
        sessionRepository.save(session);

        // 4. daily_risk_scores 계산 및 저장
        riskScoreService.calculateAndSave(
                userId,
                session.getSessionDate(),
                analysis.getDepressionScore()
        );

        // 5. 응답 반환
        return DiaryCompleteResponse.builder()
                .sessionId(sessionId)
                .emotions(EmotionScores.builder()
                        .joy(analysis.getJoy())
                        .embarrassment(analysis.getEmbarrassment())
                        .anger(analysis.getAnger())
                        .anxiety(analysis.getAnxiety())
                        .hurt(analysis.getHurt())
                        .sadness(analysis.getSadness())
                        .build())
                .depressionScore(analysis.getDepressionScore())
                .shortSummary(analysis.getShortSummary())
                .longSummary(analysis.getLongSummary())
                .emotionalAdvice(analysis.getEmotionalAdvice())
                .completedAt(session.getCompletedAt())
                .build();
    }
}
