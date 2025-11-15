package com.finger.hand_backend.counseling.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 상담 분석 요청 DTO
 */
@Data
public class CounselingAnalyzeRequest {

    private Long groupId;
    private Long userId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}
