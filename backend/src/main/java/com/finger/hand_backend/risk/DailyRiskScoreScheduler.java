package com.finger.hand_backend.risk;

import com.finger.hand_backend.diary.entity.DiarySession;
import com.finger.hand_backend.diary.entity.DiaryStatus;
import com.finger.hand_backend.diary.repository.DiarySessionRepository;
import com.finger.hand_backend.user.entity.IndividualUser;
import com.finger.hand_backend.user.repository.IndividualUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 일일 위험 점수 스케줄러
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyRiskScoreScheduler {

    private final DiarySessionRepository diarySessionRepository;
    private final IndividualUserRepository individualUserRepository;
    private final DailyRiskScoreService riskScoreService;
    private final DailyRiskScoreRepository riskScoreRepository;

    /**
     * 매일 새벽 1시, 어제 다이어리를 작성하지 않은 사용자의 하루 점수 자동 계산
     * - diary_component = null
     * - measurement_component만으로 risk_score 계산
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul") // 매일 새벽 1시
    public void calculateMissingDailyRiskScores() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("=== Missing Daily Risk Score Calculator Started (Date: {}) ===", yesterday);

        // 1. 어제 다이어리를 작성한 사용자 조회 (COMPLETED 상태만)
        Set<Long> usersWithDiary = diarySessionRepository
                .findBySessionDate(yesterday)
                .stream()
                .filter(session -> session.getStatus() == DiaryStatus.COMPLETED)
                .map(DiarySession::getUserId)
                .collect(Collectors.toSet());

        log.info("Users with completed diary yesterday: {}", usersWithDiary.size());

        // 2. 모든 사용자 조회
        List<IndividualUser> allUsers = individualUserRepository.findAll();

        int calculatedCount = 0;
        int skippedCount = 0;

        // 3. 다이어리 없는 사용자만 점수 계산
        for (IndividualUser user : allUsers) {
            Long userId = user.getUserId();

            try {
                // 3-1. 이미 다이어리 작성했으면 스킵
                if (usersWithDiary.contains(userId)) {
                    continue;
                }

                // 3-2. 이미 점수가 있으면 스킵 (중복 방지)
                boolean hasScore = riskScoreRepository
                        .findByUserIdAndScoreDate(userId, yesterday)
                        .isPresent();

                if (hasScore) {
                    skippedCount++;
                    continue;
                }

                // 3-3. 다이어리 없이 점수 계산
                riskScoreService.calculateWithoutDiary(userId, yesterday);
                calculatedCount++;

                log.debug("Calculated score without diary for user {}", userId);

            } catch (Exception e) {
                log.error("Failed to calculate score for user {}", userId, e);
            }
        }

        log.info("=== Missing Daily Risk Score Calculator Completed: {} calculated, {} skipped ===",
                calculatedCount, skippedCount);
    }
}
