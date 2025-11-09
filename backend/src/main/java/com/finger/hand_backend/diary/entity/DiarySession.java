package com.finger.hand_backend.diary.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 다이어리 세션 엔티티
 * - 메타데이터만 MySQL에 저장
 * - 실제 대화 내용은 MongoDB에 저장
 */
@Entity
@Table(
    name = "diary_sessions",
    indexes = {
        @Index(name = "idx_user_date", columnList = "user_id, session_date"),
        @Index(name = "idx_user_status", columnList = "user_id, status")
    }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiarySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * MongoDB ObjectId (diary_conversations 컬렉션 참조)
     */
    @Column(name = "mongodb_diary_id", nullable = false, length = 50)
    private String mongodbDiaryId;

    /**
     * 세션 상태
     * IN_PROGRESS: 작성 중
     * COMPLETED: 완료
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DiaryStatus status = DiaryStatus.IN_PROGRESS;

    /**
     * 질문-답변 횟수
     */
    @Column(name = "question_count")
    @Builder.Default
    private Integer questionCount = 0;

    /**
     * 세션 날짜 (daily_risk_scores 계산용)
     */
    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    /**
     * 생성 시각
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 완료 시각
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 질문 수 증가
     */
    public void incrementQuestionCount() {
        this.questionCount++;
    }

    /**
     * 세션 완료
     */
    public void complete() {
        this.status = DiaryStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
