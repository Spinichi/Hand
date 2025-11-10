package com.finger.hand_backend.report.repository;

import com.finger.hand_backend.report.entity.MonthlyReportDetail;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * 월간보고서 상세 Repository (MongoDB)
 */
public interface MonthlyReportDetailRepository extends MongoRepository<MonthlyReportDetail, String> {

    /**
     * 사용자 ID와 연도, 월로 조회
     */
    Optional<MonthlyReportDetail> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
}
