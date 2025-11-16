package com.finger.hand_backend.counseling.repository;

import com.finger.hand_backend.counseling.entity.CounselingReportDetail;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * 상담 보고서 상세 레포지토리 (MongoDB)
 */
public interface CounselingReportDetailRepository extends MongoRepository<CounselingReportDetail, String> {

    Optional<CounselingReportDetail> findById(String id);
}
