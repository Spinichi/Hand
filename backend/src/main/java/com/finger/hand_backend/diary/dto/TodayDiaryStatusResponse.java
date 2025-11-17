package com.finger.hand_backend.diary.dto;

import com.finger.hand_backend.diary.entity.DiaryStatus;
import com.finger.hand_backend.diary.entity.QuestionAnswer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 오늘의 다이어리 상태 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayDiaryStatusResponse {

    /**
     * 다이어리 상태
     * - NOT_STARTED: 아직 작성 안 함
     * - IN_PROGRESS: 작성 중
     * - COMPLETED: 완료
     */
    private DiaryStatus status;

    /**
     * 세션 ID (작성 중이거나 완료된 경우)
     */
    private Long sessionId;

    /**
     * 현재까지의 대화 내용 (작성 중인 경우만)
     */
    private List<QuestionAnswer> conversations;

    /**
     * 질문 카운트 (작성 중인 경우만)
     */
    private Integer questionCount;

    /**
     * 아직 작성 안 함
     */
    public static TodayDiaryStatusResponse notStarted() {
        return TodayDiaryStatusResponse.builder()
                .status(null) // null로 표시
                .build();
    }

    /**
     * 작성 중
     */
    public static TodayDiaryStatusResponse inProgress(Long sessionId, List<QuestionAnswer> conversations, Integer questionCount) {
        return TodayDiaryStatusResponse.builder()
                .status(DiaryStatus.IN_PROGRESS)
                .sessionId(sessionId)
                .conversations(conversations)
                .questionCount(questionCount)
                .build();
    }

    /**
     * 완료
     */
    public static TodayDiaryStatusResponse completed(Long sessionId) {
        return TodayDiaryStatusResponse.builder()
                .status(DiaryStatus.COMPLETED)
                .sessionId(sessionId)
                .build();
    }
}
