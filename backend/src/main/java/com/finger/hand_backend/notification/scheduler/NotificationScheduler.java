package com.finger.hand_backend.notification.scheduler;

import com.finger.hand_backend.diary.repository.DiaryConversationRepository;
import com.finger.hand_backend.notification.entity.NotificationType;
import com.finger.hand_backend.notification.service.NotificationService;
import com.finger.hand_backend.user.entity.IndividualUser;
import com.finger.hand_backend.user.repository.IndividualUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
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
    private final IndividualUserRepository individualUserRepository;
    private final NotificationService notificationService;

    /**
     * 매시 정각, 개인화된 알림 시간에 맞춰 다이어리 작성 리마인더 발송
     * 사용자가 설정한 notificationHour와 현재 시간이 일치하는 경우에만 발송
     */
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul") // 매시 정각 (00분)
    public void sendDiaryReminder() {
        int currentHour = LocalTime.now().getHour();
        log.info("=== Diary Reminder Scheduler Started (Hour: {}) ===", currentHour);

        LocalDate today = LocalDate.now();

        // 1. 오늘 다이어리 작성한 유저들 조회
        Set<Long> usersWithDiary = diaryConversationRepository
                .findBySessionDate(today)
                .stream()
                .map(diary -> diary.getUserId())
                .collect(Collectors.toSet());

        log.info("Users with diary today: {}", usersWithDiary.size());

        // 2. 현재 시간에 알림받을 사용자 조회 (알림 활성화 + notificationHour == 현재시간)
        List<IndividualUser> usersToNotify = individualUserRepository
                .findByDiaryReminderEnabledAndNotificationHour(true, currentHour);

        log.info("Users with notification enabled at hour {}: {}", currentHour, usersToNotify.size());

        // 3. 다이어리 작성 안 한 유저만 필터링
        List<IndividualUser> usersWithoutDiary = usersToNotify.stream()
                .filter(user -> !usersWithDiary.contains(user.getUserId()))
                .toList();

        log.info("Users without diary to notify: {}", usersWithoutDiary.size());

        // 4. 다이어리 작성 안 한 유저들에게 리마인더 발송
        for (IndividualUser user : usersWithoutDiary) {
            try {
                notificationService.sendToUser(
                        user.getUserId(),
                        NotificationType.DIARY_REMINDER,
                        "오늘 하루는 어땠나요?",
                        "감정 다이어리를 작성해보세요!"
                );
                log.debug("Sent diary reminder to user {} ({})", user.getUserId(), user.getName());
            } catch (Exception e) {
                log.error("Failed to send diary reminder to user {}", user.getUserId(), e);
            }
        }

        log.info("=== Diary Reminder Scheduler Completed ===");
    }
}
