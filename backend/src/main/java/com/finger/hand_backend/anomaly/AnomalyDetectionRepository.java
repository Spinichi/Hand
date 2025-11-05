package com.finger.hand_backend.anomaly;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AnomalyDetection Repository
 */
@Repository
public interface AnomalyDetectionRepository extends JpaRepository<AnomalyDetection, Long> {

    /**
     * 사용자별 이상치 페이징 조회 (최신순)
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 이상치 페이지
     */
    Page<AnomalyDetection> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 특정 기간 이상치 조회
     *
     * @param userId 사용자 ID
     * @param start  시작 시각
     * @param end    종료 시각
     * @return 이상치 리스트
     */
    List<AnomalyDetection> findByUserIdAndCreatedAtBetween(
        Long userId,
        LocalDateTime start,
        LocalDateTime end
    );

    /**
     * 특정 기간 이상치 개수
     *
     * @param userId 사용자 ID
     * @param start  시작 시각
     * @param end    종료 시각
     * @return 개수
     */
    long countByUserIdAndCreatedAtBetween(
        Long userId,
        LocalDateTime start,
        LocalDateTime end
    );

    /**
     * 최근 N분 이내 이상치 존재 여부 확인
     * - 중복 생성 방지용
     *
     * @param userId 사용자 ID
     * @param since  기준 시각
     * @return 존재 여부
     */
    boolean existsByUserIdAndCreatedAtAfter(Long userId, LocalDateTime since);
}
