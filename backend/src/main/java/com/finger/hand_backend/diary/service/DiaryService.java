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

    /**
     * 다이어리 시작
     */
    @Transactional
    public DiaryStartResponse startDiary(Long userId) {
        LocalDate today = LocalDate.now();

        // 1. 오늘 이미 작성한 다이어리가 있는지 확인
        sessionRepository.findByUserIdAndSessionDate(userId, today)
                .ifPresent(session -> {
                    throw new IllegalStateException("오늘 이미 다이어리를 작성했습니다");
                });

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
                .canFinish(session.getQuestionCount() >= 3) // 최소 3개 이상
                .build();
    }

    /**
     * 다이어리 완료
     */
    @Transactional
    public DiaryCompleteResponse completeDiary(Long userId, Long sessionId) {
        // 1. 세션 조회
        DiarySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다"));

        if (!session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다");
        }

        if (session.getStatus() == DiaryStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 다이어리입니다");
        }

        if (session.getQuestionCount() < 3) {
            throw new IllegalStateException("최소 3개 이상의 질문에 답변해야 합니다");
        }

        // 2. MongoDB에서 대화 조회
        DiaryConversation conversation = conversationRepository.findById(session.getMongodbDiaryId())
                .orElseThrow(() -> new IllegalStateException("대화를 찾을 수 없습니다"));

        // 3. 감정 분석 (FastAPI - 현재는 Mock)
        EmotionAnalysis analysis = emotionAnalysisClient.analyzeEmotion(
                userId,
                session.getSessionDate(),
                conversation.getQuestions()
        );

        // 4. 감정 분석 결과를 MongoDB에 저장
        conversation.setEmotionAnalysisResult(analysis);
        conversationRepository.save(conversation);

        // 5. 세션 완료
        session.complete();
        sessionRepository.save(session);

        // 6. daily_risk_scores 계산 및 저장 (내부 데이터)
        riskScoreService.calculateAndSave(
                userId,
                session.getSessionDate(),
                analysis.getDepressionScore()
        );

        log.info("다이어리 완료 - userId: {}, sessionId: {}, depressionScore: {}",
                userId, sessionId, analysis.getDepressionScore());

        // 7. 사용자에게는 감정 분석 결과와 요약 반환
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
}
