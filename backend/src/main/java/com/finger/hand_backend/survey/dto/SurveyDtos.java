package com.finger.hand_backend.survey.dto;


import com.finger.hand_backend.survey.domain.SurveyKind;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.math.BigDecimal;
import java.util.List;


public class SurveyDtos {
    public record OptionDto(short value, String label) {}


    public record AnswerDto(
            @Min(1) short questionNo,
            @Min(1) @Max(5) short choice
    ){}


    public record SubmitRequest(List<@Valid AnswerDto> answers){}


    public record ScreeningSubmitResponse(Long submissionId, int score, boolean needPss){}
    public record PssSubmitResponse(Long submissionId, int score){}


    public record ScoreResponse(short questionCount, BigDecimal avgScore, boolean finalized){}


    public record OptionsResponse(SurveyKind kind, int questionCount, List<OptionDto> options){}
}
