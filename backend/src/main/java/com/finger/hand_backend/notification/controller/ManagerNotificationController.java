package com.finger.hand_backend.notification.controller;

import com.finger.hand_backend.common.dto.ApiResponse;
import com.finger.hand_backend.notification.entity.NotificationType;
import com.finger.hand_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 컨트롤러 (관리자용)
 */
@RestController
@RequestMapping("/manager/notifications")
@RequiredArgsConstructor
@Slf4j
public class ManagerNotificationController {

    private final NotificationService notificationService;

    private Long userId(Authentication auth) {
        return Long.valueOf(auth.getName());
    }

    /**
     * 특정 유저에게 알림 전송
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendToUser(
            Authentication auth,
            @RequestBody SendNotificationRequest request) {

        Long managerId = userId(auth);
        log.info("POST /manager/notifications/send - managerId: {}, userId: {}, title: {}",
                managerId, request.getUserId(), request.getTitle());

        try {
            notificationService.sendToUser(
                    managerId,
                    request.getUserId(),
                    NotificationType.MANAGER_NOTICE,
                    request.getTitle(),
                    request.getBody()
            );

            return ResponseEntity.ok(ApiResponse.success(null, "알림이 전송되었습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("Failed to send notification: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }

    /**
     * 전체 유저에게 알림 전송 (관리자가 속한 그룹의 멤버들에게만)
     */
    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<Void>> broadcastToAll(
            Authentication auth,
            @RequestBody BroadcastNotificationRequest request) {

        Long managerId = userId(auth);
        log.info("POST /manager/notifications/broadcast - managerId: {}, title: {}", managerId, request.getTitle());

        try {
            notificationService.sendToGroupMembers(
                    managerId,
                    NotificationType.MANAGER_NOTICE,
                    request.getTitle(),
                    request.getBody()
            );

            return ResponseEntity.ok(ApiResponse.success(null, "그룹 멤버들에게 알림이 전송되었습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("Failed to broadcast notification: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }

    /**
     * 특정 유저 알림 전송 요청 DTO
     */
    @lombok.Data
    public static class SendNotificationRequest {
        private Long userId;
        private String title;
        private String body;
    }

    /**
     * 전체 알림 전송 요청 DTO
     */
    @lombok.Data
    public static class BroadcastNotificationRequest {
        private String title;
        private String body;
    }
}
