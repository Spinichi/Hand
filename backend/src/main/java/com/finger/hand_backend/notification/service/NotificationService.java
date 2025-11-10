package com.finger.hand_backend.notification.service;

import com.finger.hand_backend.notification.entity.DeviceToken;
import com.finger.hand_backend.notification.entity.Notification;
import com.finger.hand_backend.notification.entity.NotificationType;
import com.finger.hand_backend.notification.repository.DeviceTokenRepository;
import com.finger.hand_backend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 알림 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationRepository notificationRepository;
    private final FCMService fcmService;

    /**
     * 디바이스 토큰 등록
     */
    @Transactional
    public void registerDeviceToken(Long userId, String token) {
        log.info("Registering device token for user {}", userId);

        // 기존 토큰이 있는지 확인
        deviceTokenRepository.findByDeviceToken(token).ifPresentOrElse(
                existingToken -> {
                    // 토큰이 이미 존재하면 lastUsedAt만 업데이트
                    existingToken.setLastUsedAt(LocalDateTime.now());
                    existingToken.setIsActive(true);
                    deviceTokenRepository.save(existingToken);
                    log.info("Updated existing token for user {}", userId);
                },
                () -> {
                    // 새 토큰 등록
                    DeviceToken newToken = DeviceToken.builder()
                            .userId(userId)
                            .deviceToken(token)
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .lastUsedAt(LocalDateTime.now())
                            .build();
                    deviceTokenRepository.save(newToken);
                    log.info("Registered new token for user {}", userId);
                }
        );
    }

    /**
     * 디바이스 토큰 삭제
     */
    @Transactional
    public void removeDeviceToken(Long userId, String token) {
        log.info("Removing device token for user {}", userId);

        deviceTokenRepository.findByDeviceToken(token).ifPresent(deviceToken -> {
            if (deviceToken.getUserId().equals(userId)) {
                deviceToken.setIsActive(false);
                deviceTokenRepository.save(deviceToken);
                log.info("Deactivated token for user {}", userId);
            } else {
                log.warn("Token does not belong to user {}", userId);
            }
        });
    }

    /**
     * 특정 사용자에게 알림 전송
     */
    @Transactional
    public void sendToUser(Long userId, NotificationType type, String title, String body) {
        sendToUser(userId, type, title, body, null);
    }

    /**
     * 특정 사용자에게 알림 전송 (데이터 포함)
     */
    @Transactional
    public void sendToUser(Long userId, NotificationType type, String title, String body, Map<String, Object> data) {
        log.info("Sending notification to user {}: {}", userId, title);

        // 1. DB에 알림 저장
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .body(body)
                .data(data)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);

        // 2. 사용자의 활성 토큰 조회
        List<DeviceToken> tokens = deviceTokenRepository.findByUserIdAndIsActive(userId, true);

        if (tokens.isEmpty()) {
            log.warn("No active tokens for user {}", userId);
            return;
        }

        // 3. FCM 전송 (data를 String Map으로 변환)
        Map<String, String> fcmData = convertToStringMap(data);
        for (DeviceToken token : tokens) {
            boolean success = fcmService.sendToToken(token.getDeviceToken(), title, body, fcmData);

            if (!success) {
                // 전송 실패 시 토큰 비활성화
                log.warn("Failed to send to token, deactivating: {}", token.getDeviceToken());
                token.setIsActive(false);
                deviceTokenRepository.save(token);
            }
        }
    }

    /**
     * 전체 사용자에게 알림 전송
     */
    @Transactional
    public void sendToAllUsers(NotificationType type, String title, String body) {
        log.info("Sending notification to all users: {}", title);

        // 1. 모든 활성 토큰 조회
        List<DeviceToken> allTokens = deviceTokenRepository.findByIsActive(true);

        if (allTokens.isEmpty()) {
            log.warn("No active tokens found");
            return;
        }

        // 2. 각 사용자별로 DB에 알림 저장
        allTokens.stream()
                .map(DeviceToken::getUserId)
                .distinct()
                .forEach(userId -> {
                    Notification notification = Notification.builder()
                            .userId(userId)
                            .type(type)
                            .title(title)
                            .body(body)
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notification);
                });

        // 3. FCM 멀티캐스트 전송
        List<String> tokenStrings = allTokens.stream()
                .map(DeviceToken::getDeviceToken)
                .toList();

        Map<String, Boolean> results = fcmService.sendToTokens(tokenStrings, title, body, null);

        // 4. 실패한 토큰 비활성화
        results.forEach((token, success) -> {
            if (!success) {
                deviceTokenRepository.findByDeviceToken(token).ifPresent(deviceToken -> {
                    log.warn("Failed to send to token, deactivating: {}", token);
                    deviceToken.setIsActive(false);
                    deviceTokenRepository.save(deviceToken);
                });
            }
        });
    }

    /**
     * 내 알림 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<Notification> getMyNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
        log.info("Notification {} marked as read by user {}", notificationId, userId);
    }

    /**
     * 읽지 않은 알림 개수
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    /**
     * Map<String, Object>를 Map<String, String>으로 변환 (FCM data용)
     */
    private Map<String, String> convertToStringMap(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        Map<String, String> stringMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() != null) {
                stringMap.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return stringMap;
    }
}
