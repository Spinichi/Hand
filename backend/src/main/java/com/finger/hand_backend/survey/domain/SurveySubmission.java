package com.finger.hand_backend.survey.domain;


import jakarta.persistence.*;
import lombok.*;


import java.time.OffsetDateTime;


@Entity
@Table(name = "survey_submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SurveySubmission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "user_id", nullable = false)
    private Long userId;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('SCREENING','PSS')")
    private SurveyKind kind;


    @Column(nullable = false)
    private Integer score; // 해당 설문 합계 (3문항 or 5문항)


    @Column(name = "recommend_phq9")
    private Boolean recommendPhq9; // 기존 스키마 준수 (의미상 recommend_pss)


    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;
}
