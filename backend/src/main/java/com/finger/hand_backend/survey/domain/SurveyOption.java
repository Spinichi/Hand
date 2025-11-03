package com.finger.hand_backend.survey.domain;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "survey_options")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SurveyOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('SCREENING','PSS')")
    private SurveyKind kind;


    @Column(nullable = false)
    private Short value; // 1~5


    @Column(nullable = false, length = 100)
    private String label; // 전혀없음 ~ 매우자주
}
