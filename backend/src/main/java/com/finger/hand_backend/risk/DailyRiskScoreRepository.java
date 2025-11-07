package com.finger.hand_backend.risk;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 일일 위험 점수 Repository
 */
public interface DailyRiskScoreRepository extends JpaRepository<DailyRiskScore, Long> {

    /**
     * 사용자 ID와 날짜로 조회
     */
    Optional<DailyRiskScore> findByUserIdAndScoreDate(Long userId, LocalDate scoreDate);

    /**
     * 사용자 ID와 날짜 범위로 조회
     */
    List<DailyRiskScore> findByUserIdAndScoreDateBetweenOrderByScoreDateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * 사용자 ID로 최근 N일 조회
     */
    List<DailyRiskScore> findTop30ByUserIdOrderByScoreDateDesc(Long userId);
}
