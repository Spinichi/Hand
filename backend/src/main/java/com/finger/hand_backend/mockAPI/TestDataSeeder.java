package com.finger.hand_backend.mockAPI;

import com.finger.hand_backend.diary.entity.DiaryConversation;
import com.finger.hand_backend.diary.entity.EmotionAnalysis;
import com.finger.hand_backend.diary.repository.DiaryConversationRepository;
import com.finger.hand_backend.user.entity.IndividualUser;
import com.finger.hand_backend.user.entity.User;
import com.finger.hand_backend.user.repository.IndividualUserRepository;
import com.finger.hand_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TestDataSeeder {

    private final UserRepository userRepository;
    private final IndividualUserRepository individualUserRepository;
    private final DiaryConversationRepository diaryConversationRepository;
    private final PasswordEncoder passwordEncoder; // 없으면 필드 제거 + "password" 고정

    public record SeedResult(int usersCreated, int individualUsersCreated, int diariesCreated, long batchKey) {}

    public SeedResult seed(int userCount, int diariesPerUser) {
        // 지난 주(월~일) 범위 계산
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 1) User 생성 (MySQL)
        List<User> users = new ArrayList<>();
        long stamp = System.currentTimeMillis();
        for (int i = 0; i < userCount; i++) {
            User u = new User();
            u.setEmail("seed_" + stamp + "_" + i + "@test.com");
            u.setPassword(passwordEncoder != null ? passwordEncoder.encode("password") : "password");
            u.setDeleted(false);
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());
            users.add(u);
        }
        users = userRepository.saveAll(users);

        // 2) IndividualUser 생성 (MySQL) - userId를 users.id로 연결
        List<IndividualUser> individuals = new ArrayList<>();
        Random r = new Random();
        long batchKey = System.currentTimeMillis();
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);

            IndividualUser iu = IndividualUser.builder()
                    .userId(u.getId()) // ✅ 스케줄러가 쓰는 키
                    .name("SeedUser" + i)
                    .age(20 + (i % 10))
                    .gender((i % 2 == 0) ? IndividualUser.Gender.M : IndividualUser.Gender.F)
                    .job("developer")
                    .height(BigDecimal.valueOf(170.0 + (i % 10)))  // cm
                    .weight(BigDecimal.valueOf(60.0 + (i % 10)))   // kg
                    .disease("none")
                    .residenceType("alone")
                    .diaryReminderEnabled(false)
                    .notificationHour(20)
                    .name("SeedUser_" + batchKey + "_" + i)
                    .build();

            individuals.add(iu);
        }

        individuals = individualUserRepository.saveAll(individuals);

        // 3) DiaryConversation 생성 (Mongo) - userId 기준으로 지난 주에 분산
        List<DiaryConversation> diaries = new ArrayList<>();
        for (IndividualUser iu : individuals) {
            Long userId = iu.getUserId(); // ✅ Mongo도 같은 userId로 넣어야 함

            for (int d = 0; d < diariesPerUser; d++) {
                LocalDate sessionDate = weekStart.plusDays(d % 7);

                DiaryConversation dc = DiaryConversation.builder()
                        .userId(userId)
                        .sessionDate(sessionDate)
                        .questions(new ArrayList<>())
                        .emotionAnalysis(dummyEmotionAnalysis(r))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                diaries.add(dc);
            }
        }

        diaryConversationRepository.saveAll(diaries);

        return new SeedResult(users.size(), individuals.size(), diaries.size(), batchKey);
    }

    private EmotionAnalysis dummyEmotionAnalysis(Random r) {
        return EmotionAnalysis.builder()
                .joy(0.1 + r.nextDouble() * 0.2)
                .embarrassment(0.05 + r.nextDouble() * 0.2)
                .anger(0.05 + r.nextDouble() * 0.2)
                .anxiety(0.1 + r.nextDouble() * 0.2)
                .hurt(0.05 + r.nextDouble() * 0.2)
                .sadness(0.1 + r.nextDouble() * 0.3)
                .depressionScore(30.0 + r.nextDouble() * 40.0)
                .shortSummary("seed short summary")
                .longSummary("seed long summary for weekly report generation test")
                .emotionalAdvice("seed advice")
                .analyzedAt(LocalDateTime.now())
                .build();
    }
}
