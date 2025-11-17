package com.finger.hand_backend.user.dto;


import com.finger.hand_backend.user.entity.IndividualUser.Gender;
import jakarta.validation.constraints.*;


import java.math.BigDecimal;


public class IndividualUserDtos {


    public record CreateRequest(
            @NotBlank String name,
            @NotNull @Min(1) @Max(120) Integer age,
            @NotNull Gender gender,
            @NotBlank String job,
            @NotNull @DecimalMin("0.0") BigDecimal height,
            @NotNull @DecimalMin("0.0") BigDecimal weight,
            @NotBlank String disease,
            @NotBlank String residenceType,
            @NotNull Boolean diaryReminderEnabled,
            @Min(0) @Max(23) Integer notificationHour
    ) {}


    public record UpdateRequest(
            @NotBlank String name,
            @NotNull @Min(1) @Max(120) Integer age,
            @NotNull Gender gender,
            @NotBlank String job,
            @NotNull @DecimalMin("0.0") BigDecimal height,
            @NotNull @DecimalMin("0.0") BigDecimal weight,
            @NotBlank String disease,
            @NotBlank String residenceType,
            @NotNull Boolean diaryReminderEnabled,
            @Min(0) @Max(23) Integer notificationHour
    ) {}


    public record Response(
            Long id,
            Long userId,
            String name,
            Integer age,
            Gender gender,
            String job,
            BigDecimal height,
            BigDecimal weight,
            String disease,
            String residenceType,
            Boolean diaryReminderEnabled,
            Integer notificationHour
    ) {}
}
