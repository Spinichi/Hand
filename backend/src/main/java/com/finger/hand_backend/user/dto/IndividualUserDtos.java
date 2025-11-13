package com.finger.hand_backend.user.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.finger.hand_backend.user.entity.IndividualUser.Gender;
import jakarta.validation.constraints.*;


import java.math.BigDecimal;
import java.time.LocalTime;


public class IndividualUserDtos {


    public record CreateRequest(
            @NotBlank String name,
            @NotNull @Min(1) @Max(120) Integer age,
            @NotNull Gender gender,
            @NotNull @DecimalMin("0.0") BigDecimal height,
            @NotNull @DecimalMin("0.0") BigDecimal weight,
            @NotBlank String disease,
            @NotBlank String residenceType,
            @NotNull Boolean diaryReminderEnabled,
            @JsonFormat(pattern = "HH:mm") LocalTime notificationTime
    ) {}


    public record UpdateRequest(
            @NotBlank String name,
            @NotNull @Min(1) @Max(120) Integer age,
            @NotNull Gender gender,
            @NotNull @DecimalMin("0.0") BigDecimal height,
            @NotNull @DecimalMin("0.0") BigDecimal weight,
            @NotBlank String disease,
            @NotBlank String residenceType,
            @NotNull Boolean diaryReminderEnabled,
            @JsonFormat(pattern = "HH:mm") LocalTime notificationTime
    ) {}


    public record Response(
            Long id,
            Long userId,
            String name,
            Integer age,
            Gender gender,
            BigDecimal height,
            BigDecimal weight,
            String disease,
            String residenceType,
            Boolean diaryReminderEnabled,
            @JsonFormat(pattern = "HH:mm") LocalTime notificationTime
    ) {}
}
