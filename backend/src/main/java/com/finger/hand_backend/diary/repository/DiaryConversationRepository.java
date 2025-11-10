package com.finger.hand_backend.diary.repository;

import com.finger.hand_backend.diary.entity.DiaryConversation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 다이어리 대화 Repository (MongoDB)
 */
public interface DiaryConversationRepository extends MongoRepository<DiaryConversation, String> {

    /**
     * 사용자 ID로 조회
     */
    List<DiaryConversation> findByUserIdOrderBySessionDateDesc(Long userId);

    /**
     * 사용자 ID와 날짜로 조회
     */
    Optional<DiaryConversation> findByUserIdAndSessionDate(Long userId, LocalDate sessionDate);

    /**
     * 사용자 ID와 날짜 범위로 조회
     */
    List<DiaryConversation> findByUserIdAndSessionDateBetweenOrderBySessionDateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * 특정 날짜의 모든 다이어리 조회 (스케줄러용)
     */
    List<DiaryConversation> findBySessionDate(LocalDate sessionDate);
}
