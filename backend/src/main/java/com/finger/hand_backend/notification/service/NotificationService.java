package com.finger.hand_backend.notification.service;

import com.finger.hand_backend.group.entity.GroupMember;
import com.finger.hand_backend.group.entity.GroupRole;
import com.finger.hand_backend.group.repository.GroupMemberRepository;
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
import java.util.*;
import java.util.stream.Collectors;

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
    private final GroupMemberRepository groupMemberRepository;

    /**
     * 관리자가 대상 유저를 관리할 권한이 있는지 검증
     */
    private void validateManagerAccess(Long managerId, Long targetUserId) {
        List<GroupMember> managerGroups = groupMemberRepository.findByUserIdAndRole(managerId, GroupRole.MANAGER);

        if (managerGroups.isEmpty()) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }

        List<GroupMember> targetGroups = groupMemberRepository.findByUserId(targetUserId);

        Set<Long> managerGroupIds = managerGroups.stream()
                .map(gm -> gm.getGroup().getId())
                .collect(Collectors.toSet());

        boolean hasAccess = targetGroups.stream()
                .anyMatch(gm -> managerGroupIds.contains(gm.getGroup().getId()));

        if (!hasAccess) {
            throw new IllegalArgumentException("해당 사용자에 대한 접근 권한이 없습니다.");
        }

        log.debug("Manager {} has access to user {}", managerId, targetUserId);
    }

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
     * 관리자가 특정 사용자에게 알림 전송 (권한 검증 포함)
     */
    @Transactional
    public void sendToUser(Long managerId, Long userId, NotificationType type, String title, String body) {
        log.info("Manager {} sending notification to user {}: {}", managerId, userId, title);

        // 관리자 권한 검증
        validateManagerAccess(managerId, userId);

        // 기존 sendToUser 호출
        sendToUser(userId, type, title, body, null);
    }

    /**
     * 특정 사용자에게 알림 전송 (내부용 - 권한 검증 없음)
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
                // 전송 실패 로그만 남기고 토큰은 유지 (서버 문제일 수 있음)
                log.warn("Failed to send to token (token still active): {}", token.getDeviceToken());
            }
        }
    }

    /**
     * 관리자가 속한 그룹의 멤버들에게 알림 전송
     */
    @Transactional
    public void sendToGroupMembers(Long managerId, NotificationType type, String title, String body) {
        log.info("Manager {} sending notification to group members: {}", managerId, title);

        // 1. 관리자가 MANAGER인 그룹 목록 조회
        List<GroupMember> managerGroups = groupMemberRepository.findByUserIdAndRole(managerId, GroupRole.MANAGER);

        if (managerGroups.isEmpty()) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }

        // 2. 해당 그룹들의 모든 멤버 조회 (관리자 본인 제외)
        Set<Long> targetUserIds = new HashSet<>();
        for (GroupMember managerGroup : managerGroups) {
            List<GroupMember> members = groupMemberRepository.findByGroupId(managerGroup.getGroup().getId());
            members.stream()
                    .map(GroupMember::getUserId)
                    .filter(userId -> !userId.equals(managerId))  // 관리자 본인 제외
                    .forEach(targetUserIds::add);
        }

        if (targetUserIds.isEmpty()) {
            log.warn("No members found in manager's groups");
            return;
        }

        log.info("Sending to {} members", targetUserIds.size());

        // 3. 각 멤버에게 알림 전송 (권한 검증 없이)
        for (Long userId : targetUserIds) {
            sendToUser(userId, type, title, body);
        }
    }

    /**
     * 전체 사용자에게 알림 전송 (시스템용)
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

        // 4. 실패한 토큰 로그만 기록 (비활성화하지 않음)
        results.forEach((token, success) -> {
            if (!success) {
                log.warn("Failed to send to token (token still active): {}", token);
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
