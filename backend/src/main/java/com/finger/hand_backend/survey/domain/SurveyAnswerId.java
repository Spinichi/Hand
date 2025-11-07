package com.finger.hand_backend.survey.domain;


import lombok.*;


import java.io.Serializable;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class SurveyAnswerId implements Serializable {
    private Long submissionId;
    private Short questionNo;
}
