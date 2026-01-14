package com.finger.hand_backend.diary.service;

import com.finger.hand_backend.diary.dto.*;
import com.finger.hand_backend.diary.entity.*;
import com.finger.hand_backend.diary.repository.DiaryConversationRepository;
import com.finger.hand_backend.diary.repository.DiarySessionRepository;
import com.finger.hand_backend.diary.repository.QuestionPoolRepository;
import com.finger.hand_backend.risk.DailyRiskScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * 다이어리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryService {

    private final DiarySessionRepository sessionRepository;
    private final DiaryConversationRepository conversationRepository;
    private final QuestionPoolRepository questionPoolRepository;
    private final MongoTemplate mongoTemplate;

    private final GmsClient gmsClient;
    private final EmotionAnalysisClient emotionAnalysisClient;
    private final DailyRiskScoreService riskScoreService;

    // 트랜잭션 분리를 위한 별도 서비스
    private final DiaryPhase1Service phase1Service;
    private final DiaryPhase3Service phase3Service;

    /**
     * 다이어리 시작
     * - 오늘 이미 작성 중인 다이어리가 있으면 해당 세션 정보 반환
     * - 완료된 다이어리가 있으면 에러
     * - 없으면 새로 생성
     */
    @Transactional
    public DiaryStartResponse startDiary(Long userId) {
        LocalDate today = LocalDate.now();

        // 1. 오늘 이미 작성한 다이어리가 있는지 확인
        DiarySession existingSession = sessionRepository.findByUserIdAndSessionDate(userId, today)
                .orElse(null);

        if (existingSession != null) {
            // 1-1. 완료된 다이어리면 에러
            if (existingSession.getStatus() == DiaryStatus.COMPLETED) {
                throw new IllegalStateException("오늘 이미 다이어리를 작성했습니다");
            }

            // 1-2. 작성 중인 다이어리면 현재 진행 상황 반환
            DiaryConversation conversation = conversationRepository
                    .findById(existingSession.getMongodbDiaryId())
                    .orElseThrow(() -> new IllegalStateException("MongoDB 다이어리 데이터가 없습니다"));

            // 마지막 질문 찾기 (아직 답변 안 한 질문)
            QuestionAnswer lastQuestion = conversation.getQuestions().stream()
                    .filter(qa -> qa.getAnswerText() == null)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("진행 중인 질문을 찾을 수 없습니다"));

            log.info("작성 중인 다이어리 재개 - userId: {}, sessionId: {}", userId, existingSession.getId());

            return DiaryStartResponse.builder()
                    .sessionId(existingSession.getId())
                    .questionNumber(lastQuestion.getQuestionNumber())
                    .questionText(lastQuestion.getQuestionText())
                    .isResume(true) // 재개 플래그
                    .build();
        }

        // 2. 질문 풀에서 랜덤 질문 선택
        QuestionPool firstQuestion = questionPoolRepository.findRandomActiveQuestion()
                .orElseThrow(() -> new IllegalStateException("사용 가능한 질문이 없습니다"));

        // 3. MongoDB에 대화 생성 (첫 질문 포함)
        QuestionAnswer firstQA = QuestionAnswer.builder()
                .questionNumber(1)
                .questionText(firstQuestion.getQuestionText())
                .source(QuestionSource.POOL)
                .answerText(null) // 아직 답변 안 함
                .answeredAt(null)
                .build();

        DiaryConversation conversation = DiaryConversation.builder()
                .userId(userId)
                .sessionDate(today)
                .questions(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        conversation.addQuestion(firstQA);
        DiaryConversation savedConversation = conversationRepository.save(conversation);

        // 4. MySQL에 세션 메타데이터 생성
        DiarySession session = DiarySession.builder()
                .userId(userId)
                .mongodbDiaryId(savedConversation.getId())
                .status(DiaryStatus.IN_PROGRESS)
                .sessionDate(today)
                .questionCount(0)
                .build();

        DiarySession savedSession = sessionRepository.save(session);

        log.info("다이어리 시작 - userId: {}, sessionId: {}", userId, savedSession.getId());

        return DiaryStartResponse.builder()
                .sessionId(savedSession.getId())
                .questionNumber(1)
                .questionText(firstQuestion.getQuestionText())
                .isResume(false) // 명시적으로 false 설정
                .build();
    }

    /**
     * 답변 제출 & 다음 질문 받기
     */
    @Transactional
    public DiaryAnswerResponse submitAnswer(Long userId, Long sessionId, String answerText) {
        // 1. 세션 조회
        DiarySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다"));

        if (!session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다");
        }

        if (session.getStatus() == DiaryStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 다이어리입니다");
        }

        // 2. MongoDB에서 대화 조회
        DiaryConversation conversation = conversationRepository.findById(session.getMongodbDiaryId())
                .orElseThrow(() -> new IllegalStateException("대화를 찾을 수 없습니다"));

        // 3. 현재 질문 번호 (방금 답변한 질문)
        int currentQuestionNumber = session.getQuestionCount() + 1;

        // 4. 현재 질문 찾기 및 답변 저장
        QuestionAnswer currentQA = conversation.getQuestions().get(currentQuestionNumber - 1);
        currentQA.setAnswerText(answerText);
        currentQA.setAnsweredAt(LocalDateTime.now());

        conversationRepository.save(conversation);

        // 5. 질문 수 증가
        session.incrementQuestionCount();
        sessionRepository.save(session);

        // 6. GMS로 다음 질문 생성
        String nextQuestionText = gmsClient.generateNextQuestion(conversation.getQuestions());

        // 7. 다음 질문을 MongoDB에 저장 (답변은 null)
        int nextQuestionNumber = currentQuestionNumber + 1;
        QuestionAnswer nextQA = QuestionAnswer.builder()
                .questionNumber(nextQuestionNumber)
                .questionText(nextQuestionText)
                .source(QuestionSource.GMS)
                .answerText(null)
                .answeredAt(null)
                .build();

        conversation.addQuestion(nextQA);
        conversationRepository.save(conversation);

        log.info("답변 제출 - userId: {}, sessionId: {}, questionNumber: {}",
                userId, sessionId, currentQuestionNumber);

        return DiaryAnswerResponse.builder()
                .sessionId(sessionId)
                .questionNumber(nextQuestionNumber)
                .questionText(nextQuestionText)
                .canFinish(session.getQuestionCount() >= 2) // 최소 2개 이상
                .build();
    }

    /**
     * 다이어리 완료 (트랜잭션 분리 버전)
     * Phase 1: 세션 조회 및 검증 (트랜잭션) - 별도 서비스
     * Phase 2: 감정 분석 (트랜잭션 외부) - 현재 서비스
     * Phase 3: 결과 저장 및 완료 (트랜잭션) - 별도 서비스
     */
//    @Transactional
    public DiaryCompleteResponse completeDiary(Long userId, Long sessionId) {
        // Phase 1: 세션 조회 및 검증 (트랜잭션 내) - 별도 서비스로 프록시 호출
        DiaryPhase1Service.Phase1Result phase1Result = phase1Service.validateAndLoadData(userId, sessionId);

        // Phase 2: 감정 분석 (트랜잭션 외부 - 커넥션 해제 상태)
        boolean isTxActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("⚪ Phase 2 시작 - TX Active: {} (예상: false)", isTxActive);

        EmotionAnalysis analysis = emotionAnalysisClient.analyzeEmotion(
                userId,
                phase1Result.sessionDate,
                phase1Result.conversation.getQuestions()
        );

        log.info("⚪ Phase 2 완료 - TX Active: {}", TransactionSynchronizationManager.isActualTransactionActive());

        // Phase 3: 결과 저장 및 완료 (트랜잭션 내) - 별도 서비스로 프록시 호출
        return phase3Service.saveResultAndComplete(userId, sessionId, phase1Result.conversation, analysis);
    }

    /**
     * 내 다이어리 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<DiaryListResponse> getMyDiaries(Long userId, Pageable pageable) {
        Page<DiarySession> sessions = sessionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return sessions.map(session -> {
            Double depressionScore = null;
            String shortSummary = null;

            // 완료된 다이어리인 경우 우울점수와 짧은 요약 포함
            if (session.getStatus() == DiaryStatus.COMPLETED) {
                try {
                    DiaryConversation conversation = conversationRepository
                            .findById(session.getMongodbDiaryId())
                            .orElse(null);

                    if (conversation != null && conversation.getEmotionAnalysis() != null) {
                        depressionScore = conversation.getEmotionAnalysis().getDepressionScore();
                        shortSummary = conversation.getEmotionAnalysis().getShortSummary();
                    }
                } catch (Exception e) {
                    log.warn("감정 분석 결과 조회 실패 - sessionId: {}", session.getId(), e);
                }
            }

            return DiaryListResponse.builder()
                    .sessionId(session.getId())
                    .sessionDate(session.getSessionDate())
                    .status(session.getStatus())
                    .questionCount(session.getQuestionCount())
                    .createdAt(session.getCreatedAt())
                    .completedAt(session.getCompletedAt())
                    .depressionScore(depressionScore)
                    .shortSummary(shortSummary)
                    .build();
        });
    }

    /**
     * 날짜 범위로 내 다이어리 조회
     */
    @Transactional(readOnly = true)
    public Page<DiaryListResponse> getMyDiariesByDateRange(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        Page<DiarySession> sessions = sessionRepository
                .findByUserIdAndSessionDateBetweenOrderBySessionDateDesc(
                        userId, startDate, endDate, pageable
                );

        return sessions.map(session -> {
            Double depressionScore = null;
            String shortSummary = null;

            if (session.getStatus() == DiaryStatus.COMPLETED) {
                try {
                    DiaryConversation conversation = conversationRepository
                            .findById(session.getMongodbDiaryId())
                            .orElse(null);

                    if (conversation != null && conversation.getEmotionAnalysis() != null) {
                        depressionScore = conversation.getEmotionAnalysis().getDepressionScore();
                        shortSummary = conversation.getEmotionAnalysis().getShortSummary();
                    }
                } catch (Exception e) {
                    log.warn("감정 분석 결과 조회 실패 - sessionId: {}", session.getId(), e);
                }
            }

            return DiaryListResponse.builder()
                    .sessionId(session.getId())
                    .sessionDate(session.getSessionDate())
                    .status(session.getStatus())
                    .questionCount(session.getQuestionCount())
                    .createdAt(session.getCreatedAt())
                    .completedAt(session.getCompletedAt())
                    .depressionScore(depressionScore)
                    .shortSummary(shortSummary)
                    .build();
        });
    }

    /**
     * 다이어리 상세 조회
     */
    @Transactional(readOnly = true)
    public DiaryDetailResponse getDiaryDetail(Long userId, Long sessionId) {
        // 세션 조회
        DiarySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리를 찾을 수 없습니다"));

        if (!session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다");
        }

        // MongoDB에서 대화 내용 조회
        DiaryConversation conversation = conversationRepository.findById(session.getMongodbDiaryId())
                .orElseThrow(() -> new IllegalStateException("대화 내용을 찾을 수 없습니다"));

        // 완료된 경우 감정 분석 결과 포함
        EmotionScores emotions = null;
        Double depressionScore = null;
        String shortSummary = null;
        String longSummary = null;
        String emotionalAdvice = null;

        if (session.getStatus() == DiaryStatus.COMPLETED && conversation.getEmotionAnalysis() != null) {
            EmotionAnalysis analysis = conversation.getEmotionAnalysis();

            emotions = EmotionScores.builder()
                    .joy(analysis.getJoy())
                    .embarrassment(analysis.getEmbarrassment())
                    .anger(analysis.getAnger())
                    .anxiety(analysis.getAnxiety())
                    .hurt(analysis.getHurt())
                    .sadness(analysis.getSadness())
                    .build();

            depressionScore = analysis.getDepressionScore();
            shortSummary = analysis.getShortSummary();
            longSummary = analysis.getLongSummary();
            emotionalAdvice = analysis.getEmotionalAdvice();
        }

        return DiaryDetailResponse.builder()
                .sessionId(session.getId())
                .sessionDate(session.getSessionDate())
                .status(session.getStatus())
                .conversations(conversation.getQuestions())
                .emotions(emotions)
                .depressionScore(depressionScore)
                .shortSummary(shortSummary)
                .longSummary(longSummary)
                .emotionalAdvice(emotionalAdvice)
                .createdAt(session.getCreatedAt())
                .completedAt(session.getCompletedAt())
                .build();
    }

    /**
     * 오늘의 다이어리 상태 조회
     * - IN_PROGRESS: 작성 중 (sessionId, 현재까지의 대화 내용 반환)
     * - COMPLETED: 완료 (sessionId 반환)
     * - null: 아직 작성 안 함
     */
    @Transactional(readOnly = true)
    public TodayDiaryStatusResponse getTodayDiaryStatus(Long userId) {
        LocalDate today = LocalDate.now();

        DiarySession session = sessionRepository.findByUserIdAndSessionDate(userId, today)
                .orElse(null);

        // 1. 오늘 다이어리 없음
        if (session == null) {
            return TodayDiaryStatusResponse.notStarted();
        }

        // 2. 완료된 다이어리
        if (session.getStatus() == DiaryStatus.COMPLETED) {
            return TodayDiaryStatusResponse.completed(session.getId());
        }

        // 3. 작성 중인 다이어리 - MongoDB에서 현재까지의 대화 내용 조회
        DiaryConversation conversation = conversationRepository
                .findById(session.getMongodbDiaryId())
                .orElseThrow(() -> new IllegalStateException("MongoDB 다이어리 데이터가 없습니다"));

        return TodayDiaryStatusResponse.inProgress(
                session.getId(),
                conversation.getQuestions(),
                session.getQuestionCount()
        );
    }
}
