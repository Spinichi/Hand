package com.finger.hand_backend.survey.domain;


import jakarta.persistence.*;
import lombok.*;


import java.time.OffsetDateTime;


@Entity
@Table(name = "survey_answers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(SurveyAnswerId.class)
public class SurveyAnswer {
    @Id
    @Column(name = "submission_id")
    private Long submissionId;


    @Id
    @Column(name = "question_no")
    private Short questionNo;


    @Column(nullable = false)
    private Short choice; // 1~5


    @Column(name = "answered_at", nullable = false)
    private OffsetDateTime answeredAt;
}
