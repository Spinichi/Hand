package com.finger.hand_backend.survey.domain;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.math.BigDecimal;
import java.time.OffsetDateTime;


@Entity
@Table(name = "survey_scores", uniqueConstraints = {
        @UniqueConstraint(name = "uk_survey_score_user", columnNames = {"user_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SurveyScore {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "user_id", nullable = false)
    private Long userId;


    @Column(name = "screening_submission_id")
    private Long screeningSubmissionId; // nullable


    @Column(name = "pss_submission_id")
    private Long pssSubmissionId; // nullable


    @Column(name = "question_count", nullable = false)
    private Short questionCount; // 3 또는 8


    @Column(name = "avg_score", nullable = false, precision = 4, scale = 2)
    private BigDecimal avgScore; // 소수점 2자리


    @Column(nullable = false)
    private Boolean finalized = false; // 8문항 완료 시 true


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
