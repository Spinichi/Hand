package com.finger.hand_backend.survey.controller;


import com.finger.hand_backend.common.dto.ApiResponse;
import com.finger.hand_backend.survey.domain.SurveyKind;
import com.finger.hand_backend.survey.dto.SurveyDtos.*;
import com.finger.hand_backend.survey.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/surveys") // 전역 context-path /api/v1 사용 중
@RequiredArgsConstructor
public class SurveyController {
    private final SurveyService service;


    private Long userId(Authentication auth) {
        if (auth == null || auth.getName() == null) throw new IllegalStateException("Unauthorized");
        return Long.valueOf(auth.getName());
    }


    @GetMapping("/screening")
    public ResponseEntity<ApiResponse<OptionsResponse>> screeningOptions(){
        return ResponseEntity.ok(ApiResponse.success(service.getOptions(SurveyKind.SCREENING), "ok"));
    }


    @PostMapping("/screening/submit")
    public ResponseEntity<ApiResponse<ScreeningSubmitResponse>> submitScreening(Authentication auth,
                                                                                @Valid @RequestBody SubmitRequest req){
        var res = service.submitScreening(userId(auth), req);
        return ResponseEntity.ok(ApiResponse.success(res, "screening saved"));
    }


    @GetMapping("/pss")
    public ResponseEntity<ApiResponse<OptionsResponse>> pssOptions(){
        return ResponseEntity.ok(ApiResponse.success(service.getOptions(SurveyKind.PSS), "ok"));
    }


    @PostMapping("/pss/submit")
    public ResponseEntity<ApiResponse<PssSubmitResponse>> submitPss(Authentication auth,
                                                                    @Valid @RequestBody SubmitRequest req){
        var res = service.submitPss(userId(auth), req);
        return ResponseEntity.ok(ApiResponse.success(res, "pss saved"));
    }


    @GetMapping("/score/me")
    public ResponseEntity<ApiResponse<ScoreResponse>> myScore(Authentication auth){
        var res = service.getMyScore(userId(auth));
        return ResponseEntity.ok(ApiResponse.success(res, "ok"));
    }
}
