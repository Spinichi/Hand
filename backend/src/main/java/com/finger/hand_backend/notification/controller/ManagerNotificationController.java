package com.finger.hand_backend.notification.controller;

import com.finger.hand_backend.common.dto.ApiResponse;
import com.finger.hand_backend.notification.entity.NotificationType;
import com.finger.hand_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    /**
     * 특정 유저에게 알림 전송
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendToUser(@RequestBody SendNotificationRequest request) {

        log.info("POST /manager/notifications/send - userId: {}, title: {}", request.getUserId(), request.getTitle());

        notificationService.sendToUser(
                request.getUserId(),
                NotificationType.MANAGER_NOTICE,
                request.getTitle(),
                request.getBody()
        );

        return ResponseEntity.ok(ApiResponse.success(null, "알림이 전송되었습니다."));
    }

    /**
     * 전체 유저에게 알림 전송
     */
    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<Void>> broadcastToAll(@RequestBody BroadcastNotificationRequest request) {

        log.info("POST /manager/notifications/broadcast - title: {}", request.getTitle());

        notificationService.sendToAllUsers(
                NotificationType.MANAGER_NOTICE,
                request.getTitle(),
                request.getBody()
        );

        return ResponseEntity.ok(ApiResponse.success(null, "전체 알림이 전송되었습니다."));
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
