package com.finger.hand_backend.diary.repository;

import com.finger.hand_backend.diary.entity.DiarySession;
import com.finger.hand_backend.diary.entity.DiaryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 다이어리 세션 Repository
 */
public interface DiarySessionRepository extends JpaRepository<DiarySession, Long> {

    /**
     * 사용자 ID와 상태로 조회
     */
    Page<DiarySession> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            DiaryStatus status,
            Pageable pageable
    );

    /**
     * 사용자 ID로 전체 조회 (페이징)
     */
    Page<DiarySession> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자 ID와 날짜로 조회
     */
    Optional<DiarySession> findByUserIdAndSessionDate(Long userId, LocalDate sessionDate);

    /**
     * 사용자 ID와 MongoDB ID로 조회
     */
    Optional<DiarySession> findByUserIdAndMongodbDiaryId(Long userId, String mongodbDiaryId);
}
