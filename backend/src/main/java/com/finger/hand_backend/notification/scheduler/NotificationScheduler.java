package com.finger.hand_backend.notification.scheduler;

import com.finger.hand_backend.diary.repository.DiaryConversationRepository;
import com.finger.hand_backend.notification.entity.NotificationType;
import com.finger.hand_backend.notification.repository.DeviceTokenRepository;
import com.finger.hand_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 알림 스케줄러
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final DiaryConversationRepository diaryConversationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationService notificationService;

    /**
     * 매일 저녁 8시, 다이어리 작성 리마인더 발송
     */
    @Scheduled(cron = "0 0 20 * * *", zone = "Asia/Seoul")
    public void sendDiaryReminder() {
        log.info("=== Diary Reminder Scheduler Started ===");

        LocalDate today = LocalDate.now();

        // 1. 오늘 다이어리 작성한 유저들 조회
        Set<Long> usersWithDiary = diaryConversationRepository
                .findBySessionDate(today)
                .stream()
                .map(diary -> diary.getUserId())
                .collect(Collectors.toSet());

        log.info("Users with diary today: {}", usersWithDiary.size());

        // 2. 토큰이 등록된 모든 유저 조회
        Set<Long> allActiveUsers = deviceTokenRepository.findByIsActive(true)
                .stream()
                .map(token -> token.getUserId())
                .collect(Collectors.toSet());

        log.info("All active users: {}", allActiveUsers.size());

        // 3. 다이어리 작성 안 한 유저 = 전체 유저 - 다이어리 작성한 유저
        List<Long> usersWithoutDiary = allActiveUsers.stream()
                .filter(userId -> !usersWithDiary.contains(userId))
                .toList();

        log.info("Users without diary: {}", usersWithoutDiary.size());

        // 4. 다이어리 작성 안 한 유저들에게 리마인더 발송
        for (Long userId : usersWithoutDiary) {
            try {
                notificationService.sendToUser(
                        userId,
                        NotificationType.DIARY_REMINDER,
                        "오늘 하루는 어땠나요?",
                        "감정 다이어리를 작성해보세요!"
                );
                log.debug("Sent diary reminder to user {}", userId);
            } catch (Exception e) {
                log.error("Failed to send diary reminder to user {}", userId, e);
            }
        }

        log.info("=== Diary Reminder Scheduler Completed ===");
    }
}
