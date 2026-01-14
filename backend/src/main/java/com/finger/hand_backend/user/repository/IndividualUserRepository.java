package com.finger.hand_backend.user.repository;


import com.finger.hand_backend.user.entity.IndividualUser;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;


public interface IndividualUserRepository extends JpaRepository<IndividualUser, Integer> {
    Optional<IndividualUser> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    void deleteByUserId(Long userId);
    List<IndividualUser> findByNameStartingWith(String prefix);

    /**
     * 알림 활성화되어 있고, 특정 시간에 알림받을 사용자 목록 조회
     */
    List<IndividualUser> findByDiaryReminderEnabledAndNotificationHour(Boolean enabled, Integer hour);
}
