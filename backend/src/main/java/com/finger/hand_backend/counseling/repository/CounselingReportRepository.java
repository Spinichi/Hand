package com.finger.hand_backend.counseling.repository;

import com.finger.hand_backend.counseling.entity.CounselingReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 상담 보고서 Repository
 */
@Repository
public interface CounselingReportRepository extends JpaRepository<CounselingReport, Long> {

    /**
     * 특정 유저의 상담 보고서 목록 조회 (최신순, 페이징)
     */
    Page<CounselingReport> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 특정 유저의 최신 상담 보고서 조회
     */
    Optional<CounselingReport> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 유저의 특정 기간 상담 보고서 조회 (중복 체크용)
     */
    Optional<CounselingReport> findTopByUserIdAndStartDateAndEndDateOrderByCreatedAtDesc(
            Long userId, LocalDate startDate, LocalDate endDate);
}
