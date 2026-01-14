package com.finger.hand_backend.diary.controller;

import com.finger.hand_backend.common.dto.ApiResponse;
import com.finger.hand_backend.diary.dto.*;
import com.finger.hand_backend.diary.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 다이어리 Controller
 * - 감정 일기 작성 API
 */
@RestController
@RequestMapping("/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /**
     * 다이어리 시작
     * POST /api/v1/diaries/start
     *
     * @return 첫 질문
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<DiaryStartResponse>> startDiary(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());

        DiaryStartResponse response = diaryService.startDiary(userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "다이어리를 시작했습니다"));
    }

    /**
     * 답변 제출 & 다음 질문 받기
     * POST /api/v1/diaries/{sessionId}/answer
     *
     * @param sessionId 세션 ID
     * @param request   답변
     * @return 다음 질문
     */
    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<ApiResponse<DiaryAnswerResponse>> submitAnswer(
            Authentication authentication,
            @PathVariable Long sessionId,
            @Valid @RequestBody DiaryAnswerRequest request
    ) {
        Long userId = Long.valueOf(authentication.getName());

        DiaryAnswerResponse response = diaryService.submitAnswer(
                userId, sessionId, request.getAnswerText());

        return ResponseEntity.ok(ApiResponse.success(response, "답변이 제출되었습니다"));
    }

    /**
     * 다이어리 완료
     * POST /api/v1/diaries/{sessionId}/complete
     *
     * @param sessionId 세션 ID
     * @return 감정 분석 결과
     */
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<ApiResponse<DiaryCompleteResponse>> completeDiary(
            Authentication authentication,
            @PathVariable Long sessionId
    ) {
        // 성능 테스트용: 인증 없으면 userId=1 사용
        Long userId = (authentication != null) ? Long.valueOf(authentication.getName()) : 1L;

        DiaryCompleteResponse response = diaryService.completeDiary(userId, sessionId);

        return ResponseEntity.ok(ApiResponse.success(response, "다이어리 작성이 완료되었습니다"));
    }

    /**
     * 내 다이어리 목록 조회
     * GET /api/v1/diaries/my
     *
     * @param pageable 페이징 정보
     * @param startDate 시작 날짜 (optional)
     * @param endDate 종료 날짜 (optional)
     * @return 다이어리 목록
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<DiaryListResponse>>> getMyDiaries(
            Authentication authentication,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Page<DiaryListResponse> diaries;

        // 날짜 범위가 지정된 경우
        if (startDate != null && endDate != null) {
            diaries = diaryService.getMyDiariesByDateRange(
                    userId,
                    java.time.LocalDate.parse(startDate),
                    java.time.LocalDate.parse(endDate),
                    pageable
            );
        } else {
            // 전체 조회
            diaries = diaryService.getMyDiaries(userId, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(diaries,"내 다이어리 목록 조회"));
    }

    /**
     * 다이어리 상세 조회
     * GET /api/v1/diaries/{sessionId}
     *
     * @param sessionId 세션 ID
     * @return 다이어리 상세 (전체 대화 내용 포함)
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<DiaryDetailResponse>> getDiaryDetail(
            Authentication authentication,
            @PathVariable Long sessionId
    ) {
        Long userId = Long.valueOf(authentication.getName());

        DiaryDetailResponse detail = diaryService.getDiaryDetail(userId, sessionId);

        return ResponseEntity.ok(ApiResponse.success(detail, "다이어리 상세 조회"));
    }

    /**
     * 오늘의 다이어리 상태 조회
     * GET /api/v1/diaries/today
     *
     * @return 오늘의 다이어리 상태 (NOT_STARTED, IN_PROGRESS, COMPLETED)
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayDiaryStatusResponse>> getTodayDiaryStatus(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());

        TodayDiaryStatusResponse response = diaryService.getTodayDiaryStatus(userId);

        String message;
        if (response.getStatus() == null) {
            message = "오늘 작성한 다이어리가 없습니다";
        } else if (response.getStatus() == com.finger.hand_backend.diary.entity.DiaryStatus.IN_PROGRESS) {
            message = "작성 중인 다이어리가 있습니다";
        } else {
            message = "오늘 다이어리 작성을 완료했습니다";
        }

        return ResponseEntity.ok(ApiResponse.success(response, message));
    }
}
