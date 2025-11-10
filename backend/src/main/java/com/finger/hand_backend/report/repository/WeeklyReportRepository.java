package com.finger.hand_backend.report.repository;

import com.finger.hand_backend.report.entity.WeeklyReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 주간보고서 Repository
 */
public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {

    /**
     * 사용자 ID와 연도, 주차로 조회
     */
    Optional<WeeklyReport> findByUserIdAndYearAndWeekNumber(Long userId, Integer year, Integer weekNumber);

    /**
     * 사용자 ID로 조회 (페이징)
     */
    Page<WeeklyReport> findByUserIdOrderByYearDescWeekNumberDesc(Long userId, Pageable pageable);

    /**
     * 사용자 ID와 연도로 조회 (페이징)
     */
    Page<WeeklyReport> findByUserIdAndYearOrderByWeekNumberDesc(Long userId, Integer year, Pageable pageable);

    /**
     * 최신 보고서 조회
     */
    Optional<WeeklyReport> findTopByUserIdOrderByYearDescWeekNumberDesc(Long userId);
}
