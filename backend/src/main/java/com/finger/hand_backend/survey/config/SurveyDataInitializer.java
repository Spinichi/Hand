package com.finger.hand_backend.survey.config;


import com.finger.hand_backend.survey.domain.*;
import com.finger.hand_backend.survey.repository.SurveyOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;


import java.util.List;


@Configuration
@RequiredArgsConstructor
public class SurveyDataInitializer implements CommandLineRunner {
    private final SurveyOptionRepository optionRepo;


    @Override
    public void run(String... args) {
        if (optionRepo.findByKindOrderByValueAsc(SurveyKind.SCREENING).isEmpty()) {
            optionRepo.saveAll(List.of(
                    SurveyOption.builder().kind(SurveyKind.SCREENING).value((short)1).label("전혀없음").build(),
                    SurveyOption.builder().kind(SurveyKind.SCREENING).value((short)2).label("거의없음").build(),
                    SurveyOption.builder().kind(SurveyKind.SCREENING).value((short)3).label("때때로있음").build(),
                    SurveyOption.builder().kind(SurveyKind.SCREENING).value((short)4).label("자주있음").build(),
                    SurveyOption.builder().kind(SurveyKind.SCREENING).value((short)5).label("매우자주").build()
            ));
        }
        if (optionRepo.findByKindOrderByValueAsc(SurveyKind.PSS).isEmpty()) {
            optionRepo.saveAll(List.of(
                    SurveyOption.builder().kind(SurveyKind.PSS).value((short)1).label("전혀없음").build(),
                    SurveyOption.builder().kind(SurveyKind.PSS).value((short)2).label("거의없음").build(),
                    SurveyOption.builder().kind(SurveyKind.PSS).value((short)3).label("때때로있음").build(),
                    SurveyOption.builder().kind(SurveyKind.PSS).value((short)4).label("자주있음").build(),
                    SurveyOption.builder().kind(SurveyKind.PSS).value((short)5).label("매우자주").build()
            ));
        }
    }
}
