package com.finger.hand_backend.report.repository;

import com.finger.hand_backend.report.entity.WeeklyReportDetail;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * 주간보고서 상세 Repository (MongoDB)
 */
public interface WeeklyReportDetailRepository extends MongoRepository<WeeklyReportDetail, String> {

    /**
     * 사용자 ID와 연도, 주차로 조회
     */
    Optional<WeeklyReportDetail> findByUserIdAndYearAndWeekNumber(Long userId, Integer year, Integer weekNumber);
}
