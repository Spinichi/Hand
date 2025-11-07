package com.finger.hand_backend.diary.controller;

import com.finger.hand_backend.common.dto.ApiResponse;
import com.finger.hand_backend.diary.dto.*;
import com.finger.hand_backend.diary.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        Long userId = Long.valueOf(authentication.getName());

        DiaryCompleteResponse response = diaryService.completeDiary(userId, sessionId);

        return ResponseEntity.ok(ApiResponse.success(response, "다이어리 작성이 완료되었습니다"));
    }

    /**
     * 내 다이어리 목록 조회
     * GET /api/v1/diaries/my
     *
     * @param pageable 페이징 정보
     * @return 다이어리 목록
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<DiaryListResponse>>> getMyDiaries(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Page<DiaryListResponse> diaries = diaryService.getMyDiaries(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(diaries,"내 다이어리 목록 조회"));
    }
}
