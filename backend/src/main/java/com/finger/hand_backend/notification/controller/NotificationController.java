package com.finger.hand_backend.notification.controller;

import com.finger.hand_backend.common.dto.ApiResponse;
import com.finger.hand_backend.notification.entity.Notification;
import com.finger.hand_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 컨트롤러 (사용자용)
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 디바이스 토큰 등록
     */
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<Void>> registerToken(
            Authentication authentication,
            @RequestBody TokenRequest request) {

        Long userId = Long.valueOf(authentication.getName());
        log.info("POST /notifications/token - userId: {}", userId);

        notificationService.registerDeviceToken(userId, request.getToken());
        return ResponseEntity.ok(ApiResponse.success(null, "디바이스 토큰이 등록되었습니다."));
    }

    /**
     * 디바이스 토큰 삭제
     */
    @DeleteMapping("/token")
    public ResponseEntity<ApiResponse<Void>> removeToken(
            Authentication authentication,
            @RequestBody TokenRequest request) {

        Long userId = Long.valueOf(authentication.getName());
        log.info("DELETE /notifications/token - userId: {}", userId);

        notificationService.removeDeviceToken(userId, request.getToken());
        return ResponseEntity.ok(ApiResponse.success(null, "디바이스 토큰이 삭제되었습니다."));
    }

    /**
     * 내 알림 목록 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Notification>>> getMyNotifications(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long userId = Long.valueOf(authentication.getName());
        log.info("GET /notifications - userId: {}", userId);

        Page<Notification> notifications = notificationService.getMyNotifications(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications, "알림 목록 조회 성공"));
    }

    /**
     * 읽지 않은 알림 개수
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());
        log.info("GET /notifications/unread-count - userId: {}", userId);

        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count, "읽지 않은 알림 개수 조회 성공"));
    }

    /**
     * 알림 읽음 처리
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            Authentication authentication,
            @PathVariable Long notificationId) {

        Long userId = Long.valueOf(authentication.getName());
        log.info("PATCH /notifications/{}/read - userId: {}", notificationId, userId);

        notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok(ApiResponse.success(null, "알림을 읽음 처리했습니다."));
    }

    /**
     * 토큰 요청 DTO
     */
    @lombok.Data
    public static class TokenRequest {
        private String token;
    }
}
