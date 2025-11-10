package com.finger.hand_backend.report.repository;

import com.finger.hand_backend.report.entity.MonthlyReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 월간보고서 Repository
 */
public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {

    /**
     * 사용자 ID와 연도, 월로 조회
     */
    Optional<MonthlyReport> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);

    /**
     * 사용자 ID로 조회 (페이징)
     */
    Page<MonthlyReport> findByUserIdOrderByYearDescMonthDesc(Long userId, Pageable pageable);

    /**
     * 사용자 ID와 연도로 조회 (페이징)
     */
    Page<MonthlyReport> findByUserIdAndYearOrderByMonthDesc(Long userId, Integer year, Pageable pageable);

    /**
     * 최신 보고서 조회
     */
    Optional<MonthlyReport> findTopByUserIdOrderByYearDescMonthDesc(Long userId);
}
