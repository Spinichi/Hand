package com.finger.hand_backend.sleep;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Sleep Repository
 * - 수면 데이터 조회 메서드
 */
@Repository
public interface SleepRepository extends JpaRepository<Sleep, Long> {

    /**
     * 특정 날짜의 수면 데이터 조회
     *
     * @param userId    사용자 ID
     * @param sleepDate 수면 날짜
     * @return 해당 날짜의 수면 데이터
     */
    Optional<Sleep> findByUserIdAndSleepDate(Long userId, LocalDate sleepDate);

    /**
     * 사용자의 모든 수면 데이터 조회 (최신순)
     *
     * @param userId 사용자 ID
     * @return 수면 데이터 리스트
     */
    List<Sleep> findByUserIdOrderBySleepDateDesc(Long userId);

    /**
     * 특정 기간의 수면 데이터 조회
     *
     * @param userId    사용자 ID
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @return 수면 데이터 리스트
     */
    List<Sleep> findByUserIdAndSleepDateBetweenOrderBySleepDateDesc(
        Long userId,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * 오늘의 수면 데이터 존재 여부
     *
     * @param userId    사용자 ID
     * @param sleepDate 수면 날짜
     * @return 존재 여부
     */
    boolean existsByUserIdAndSleepDate(Long userId, LocalDate sleepDate);
}