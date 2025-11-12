package com.finger.hand_backend.measurement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Measurement Repository
 * - 측정 데이터 조회 메서드
 */
@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    /**
     * 사용자별 측정 데이터 페이징 조회
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 측정 데이터 페이지
     */
    Page<Measurement> findByUserId(Long userId, Pageable pageable);

    /**
     * 사용자별 특정 기간 측정 데이터 조회
     * Baseline 계산용
     *
     * @param userId 사용자 ID
     * @param start  시작 시간
     * @param end    종료 시간
     * @return 측정 데이터 리스트
     */
    List<Measurement> findByUserIdAndMeasuredAtBetween(
        Long userId,
        LocalDateTime start,
        LocalDateTime end
    );

    /**
     * 편안한 상태 데이터만 조회 (stressLevel ≤ maxLevel)
     * Baseline 계산 시 편안한 상태 데이터만 사용
     *
     * @param userId         사용자 ID
     * @param start          시작 시간
     * @param end            종료 시간
     * @param maxStressLevel 최대 스트레스 레벨 (보통 2)
     * @return 편안한 상태 측정 데이터 리스트
     */
    List<Measurement> findByUserIdAndMeasuredAtBetweenAndStressLevelLessThanEqual(
        Long userId,
        LocalDateTime start,
        LocalDateTime end,
        Integer maxStressLevel
    );

    /**
     * 사용자의 측정 데이터 개수 조회
     *
     * @param userId 사용자 ID
     * @return 측정 데이터 개수
     */
    long countByUserId(Long userId);

    /**
     * 특정 기간 측정 데이터 개수 조회
     *
     * @param userId 사용자 ID
     * @param start  시작 시간
     * @param end    종료 시간
     * @return 측정 데이터 개수
     */
    long countByUserIdAndMeasuredAtBetween(
        Long userId,
        LocalDateTime start,
        LocalDateTime end
    );

    /**
     * 이전 측정 데이터 조회 (totalSteps 있는 가장 최근 데이터)
     * stepsPerMinute 계산용
     *
     * @param userId 사용자 ID
     * @return 이전 측정 데이터 (없으면 Optional.empty())
     */
    Optional<Measurement> findTop1ByUserIdAndTotalStepsIsNotNullOrderByMeasuredAtDesc(Long userId);

    /**
     * 이상치 데이터 조회 (isAnomaly=true)
     * 보고서 생성 시 워치에서 탐지된 이상치 조회
     *
     * @param userId 사용자 ID
     * @param start  시작 시간
     * @param end    종료 시간
     * @return 이상치 측정 데이터 리스트
     */
    List<Measurement> findByUserIdAndIsAnomalyTrueAndMeasuredAtBetweenOrderByMeasuredAtAsc(
        Long userId,
        LocalDateTime start,
        LocalDateTime end
    );

    // 시작시각 직전(=포함) 가장 최근 1건 — 결정적 정렬
    @Query("""
  SELECT m FROM Measurement m
  WHERE m.userId = :userId
    AND m.measuredAt <= :t
  ORDER BY m.measuredAt DESC, m.id DESC
""")
    List<Measurement> findLatestAtOrBefore(Long userId, LocalDateTime t, Pageable pageable);

    // 종료 이후 ~ 데드라인 사이 가장 이른 1건 — 결정적 정렬
    @Query("""
  SELECT m FROM Measurement m
  WHERE m.userId = :userId
    AND m.measuredAt >= :from
    AND m.measuredAt <= :to
  ORDER BY m.measuredAt ASC, m.id ASC
""")
    List<Measurement> findFirstBetween(Long userId, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
