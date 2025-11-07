package com.finger.hand_backend.survey.service;


import com.finger.hand_backend.survey.domain.*;
import com.finger.hand_backend.survey.dto.SurveyDtos.*;
import com.finger.hand_backend.survey.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {
    private final SurveyOptionRepository optionRepo;
    private final SurveySubmissionRepository submissionRepo;
    private final SurveyAnswerRepository answerRepo;
    private final SurveyScoreRepository scoreRepo;


    public OptionsResponse getOptions(SurveyKind kind) {
        int qCount = (kind == SurveyKind.SCREENING) ? 3 : 5;
        var options = optionRepo.findByKindOrderByValueAsc(kind)
                .stream().map(o -> new OptionDto(o.getValue().shortValue(), o.getLabel()))
                .collect(Collectors.toList());
        return new OptionsResponse(kind, qCount, options);
    }


    @Transactional
    public ScreeningSubmitResponse submitScreening(Long userId, @Valid SubmitRequest req) {
        int sum = req.answers().stream().mapToInt(a -> a.choice()).sum();
// 1) submissions 저장
        SurveySubmission sub = submissionRepo.save(SurveySubmission.builder()
                .userId(userId)
                .kind(SurveyKind.SCREENING)
                .score(sum)
                .recommendPhq9(sum >= 9)
                .submittedAt(OffsetDateTime.now())
                .build());
// 2) answers 저장
        OffsetDateTime now = OffsetDateTime.now();
        List<SurveyAnswer> answers = req.answers().stream()
                .map(a -> SurveyAnswer.builder()
                        .submissionId(sub.getId())
                        .questionNo(a.questionNo())
                        .choice(a.choice())
                        .answeredAt(now)
                        .build())
                .toList();
        answerRepo.saveAll(answers);


// 3) scores UPSERT (3문항 평균)
        BigDecimal avg3 = BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
        SurveyScore score = scoreRepo.findByUserId(userId)
                .orElse(SurveyScore.builder().userId(userId).build());
        score.setScreeningSubmissionId(sub.getId());
        score.setQuestionCount((short)3);
        score.setAvgScore(avg3);
        score.setFinalized(false);
        scoreRepo.save(score);


        return new ScreeningSubmitResponse(sub.getId(), sum, sum >= 9);
    }

    @Transactional
    public PssSubmitResponse submitPss(Long userId, @Valid SubmitRequest req) {
        int sum5 = req.answers().stream().mapToInt(a -> a.choice()).sum();


// 최신 SCREENING 점수 구하기 (필수)
        SurveySubmission latestScreening = submissionRepo
                .findTopByUserIdAndKindOrderBySubmittedAtDesc(userId, SurveyKind.SCREENING)
                .orElseThrow(() -> new IllegalStateException("스크리닝 제출 이력이 필요합니다."));


        int total = latestScreening.getScore() + sum5; // 3 + 5 = 8문항 합계


// 1) PSS submissions 저장
        SurveySubmission sub = submissionRepo.save(SurveySubmission.builder()
                .userId(userId)
                .kind(SurveyKind.PSS)
                .score(sum5)
                .recommendPhq9(false)
                .submittedAt(OffsetDateTime.now())
                .build());


// 2) answers 저장
        OffsetDateTime now = OffsetDateTime.now();
        List<SurveyAnswer> answers = req.answers().stream()
                .map(a -> SurveyAnswer.builder()
                        .submissionId(sub.getId())
                        .questionNo(a.questionNo())
                        .choice(a.choice())
                        .answeredAt(now)
                        .build())
                .toList();
        answerRepo.saveAll(answers);


// 3) scores 갱신 (8문항 평균)
        BigDecimal avg8 = BigDecimal.valueOf(total)
                .divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);
        SurveyScore score = scoreRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("survey_scores 레코드가 없습니다."));
        score.setPssSubmissionId(sub.getId());
        score.setQuestionCount((short)8);
        score.setAvgScore(avg8);
        score.setFinalized(true);
        scoreRepo.save(score);


        return new PssSubmitResponse(sub.getId(), sum5);
    }


    public ScoreResponse getMyScore(Long userId) {
        SurveyScore score = scoreRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("점수 기록이 없습니다."));
        return new ScoreResponse(score.getQuestionCount(), score.getAvgScore(), score.getFinalized());
    }
}