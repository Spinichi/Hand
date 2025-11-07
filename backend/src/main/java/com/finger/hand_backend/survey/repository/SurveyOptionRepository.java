package com.finger.hand_backend.survey.repository;


import com.finger.hand_backend.survey.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;


public interface SurveyOptionRepository extends JpaRepository<SurveyOption, Long> {
    List<SurveyOption> findByKindOrderByValueAsc(SurveyKind kind);
}
