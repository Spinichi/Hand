package com.finger.hand_backend.survey.repository;


import com.finger.hand_backend.survey.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface SurveySubmissionRepository extends JpaRepository<SurveySubmission, Long> {
    Optional<SurveySubmission> findTopByUserIdAndKindOrderBySubmittedAtDesc(Long userId, SurveyKind kind);
}
