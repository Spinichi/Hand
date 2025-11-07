package com.finger.hand_backend.survey.repository;


import com.finger.hand_backend.survey.domain.SurveyScore;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface SurveyScoreRepository extends JpaRepository<SurveyScore, Long> {
    Optional<SurveyScore> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
