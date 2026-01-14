package com.finger.hand_backend.diary.service;

import com.finger.hand_backend.diary.entity.DiaryConversation;
import com.finger.hand_backend.diary.entity.DiarySession;
import com.finger.hand_backend.diary.repository.DiaryConversationRepository;
import com.finger.hand_backend.diary.repository.DiarySessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;

/**
 * 다이어리 Phase 1 서비스
 * - 트랜잭션 분리를 위해 별도 서비스로 분리
 * - Self-invocation 문제 해결
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryPhase1Service {

    private final DiarySessionRepository sessionRepository;
    private final DiaryConversationRepository conversationRepository;

    /**
     * Phase 1 결과를 담는 DTO
     */
    public static class Phase1Result {
        public DiaryConversation conversation;
        public LocalDate sessionDate;

        public Phase1Result(DiaryConversation conversation, LocalDate sessionDate) {
            this.conversation = conversation;
            this.sessionDate = sessionDate;
        }
    }

    /**
     * Phase 1: 세션 조회 및 검증 (트랜잭션)
     */
    @Transactional(readOnly = true)
    public Phase1Result validateAndLoadData(Long userId, Long sessionId) {

        // 1. 세션 조회
        DiarySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다" + sessionId));

        boolean isTxActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("🔵 Phase 1 - sessionId: {}, TX Active: {}", sessionId, isTxActive);

        if (!session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다");
        }

        // 성능 테스트용: 중복 완료 검증 임시 비활성화
        // if (session.getStatus() == DiaryStatus.COMPLETED) {
        //     throw new IllegalStateException("이미 완료된 다이어리입니다");
        // }

        if (session.getQuestionCount() < 2) {
            throw new IllegalStateException("최소 2개 이상의 질문에 답변해야 합니다");
        }

        // 2. MongoDB에서 대화 조회
        String mongoId = session.getMongodbDiaryId();
        log.info("🔍 MongoDB 조회 시도 - mongoId: {}", mongoId);
        DiaryConversation conversation = conversationRepository.findById(mongoId)
                .orElseThrow(() -> new IllegalStateException("대화를 찾을 수 없습니다: " + mongoId));

        // sessionDate를 함께 반환 (Phase 2에서 DB 조회 제거)
        return new Phase1Result(conversation, session.getSessionDate());
    }
}
