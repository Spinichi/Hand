package com.finger.hand_backend.baseline;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Baseline Repository
 */
public interface BaselineRepository extends JpaRepository<Baseline, Long> {

    /**
     * 사용자의 활성 Baseline 조회
     *
     * @param userId 사용자 ID
     * @return 활성 Baseline
     */
    Optional<Baseline> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * 사용자의 Baseline 이력 조회 (페이징)
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return Baseline 페이지
     */
    Page<Baseline> findByUserIdOrderByVersionDesc(Long userId, Pageable pageable);

    /**
     * 사용자의 특정 버전 Baseline 조회
     *
     * @param userId  사용자 ID
     * @param version 버전 번호
     * @return Baseline
     */
    Optional<Baseline> findByUserIdAndVersion(Long userId, Integer version);

    /**
     * 사용자의 최신 버전 번호 조회
     *
     * @param userId 사용자 ID
     * @return 최신 버전 번호 (없으면 0)
     */
    @Query("SELECT COALESCE(MAX(b.version), 0) FROM Baseline b WHERE b.userId = :userId")
    Integer findMaxVersionByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 모든 Baseline 비활성화
     * (새 Baseline 활성화 전 호출)
     *
     * @param userId 사용자 ID
     */
    @Modifying
    @Query("UPDATE Baseline b SET b.isActive = false WHERE b.userId = :userId")
    void deactivateAllByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 Baseline 개수 조회
     *
     * @param userId 사용자 ID
     * @return Baseline 개수
     */
    long countByUserId(Long userId);
}
