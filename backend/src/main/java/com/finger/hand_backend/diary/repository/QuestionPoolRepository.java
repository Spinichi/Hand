package com.finger.hand_backend.diary.repository;

import com.finger.hand_backend.diary.entity.QuestionPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 질문 풀 Repository
 */
public interface QuestionPoolRepository extends JpaRepository<QuestionPool, Long> {

    /**
     * 활성화된 질문만 조회
     */
    List<QuestionPool> findByIsActiveTrueOrderByIdAsc();

    /**
     * 랜덤으로 활성화된 질문 1개 선택
     */
    @Query(value = "SELECT * FROM question_pools WHERE is_active = true ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<QuestionPool> findRandomActiveQuestion();
}
