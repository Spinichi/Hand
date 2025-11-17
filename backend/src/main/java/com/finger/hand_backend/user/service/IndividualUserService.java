package com.finger.hand_backend.user.service;

import com.finger.hand_backend.user.entity.IndividualUser;
import com.finger.hand_backend.user.dto.IndividualUserDtos.*;
import com.finger.hand_backend.user.repository.IndividualUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndividualUserService {
    private final IndividualUserRepository repo;


    @Transactional
    public Response create(Long currentUserId, CreateRequest req) {
        if (repo.existsByUserId(currentUserId)) {
            throw new IllegalStateException("Profile already exists for this user");
        }

        // notificationHour가 null이면 기본값 20시로 설정
        Integer notificationHour = req.notificationHour() != null ? req.notificationHour() : 20;

        IndividualUser saved = repo.save(IndividualUser.builder()
                .userId(currentUserId)
                .name(req.name())
                .age(req.age())
                .gender(req.gender())
                .job(req.job())
                .height(req.height())
                .weight(req.weight())
                .disease(req.disease())
                .residenceType(req.residenceType())
                .diaryReminderEnabled(req.diaryReminderEnabled())
                .notificationHour(notificationHour)
                .build());
        return toResponse(saved);
    }


    public Response getMine(Long currentUserId) {
        IndividualUser iu = repo.findByUserId(currentUserId)
                .orElseThrow(() -> new IllegalStateException("Profile not found"));
        return toResponse(iu);
    }


    @Transactional
    public Response update(Long currentUserId, UpdateRequest req) {
        IndividualUser iu = repo.findByUserId(currentUserId)
                .orElseThrow(() -> new IllegalStateException("Profile not found"));

        // notificationHour가 null이면 기본값 20시로 설정
        Integer notificationHour = req.notificationHour() != null ? req.notificationHour() : 20;

        iu.setName(req.name());
        iu.setAge(req.age());
        iu.setGender(req.gender());
        iu.setJob(req.job());
        iu.setHeight(req.height());
        iu.setWeight(req.weight());
        iu.setDisease(req.disease());
        iu.setResidenceType(req.residenceType());
        iu.setDiaryReminderEnabled(req.diaryReminderEnabled());
        iu.setNotificationHour(notificationHour);
        return toResponse(iu);
    }


    @Transactional
    public void deleteMine(Long currentUserId) {
        if (!repo.existsByUserId(currentUserId)) return; // idempotent
        repo.deleteByUserId(currentUserId);
    }


    private static Response toResponse(IndividualUser iu) {
        return new Response(
                iu.getId(), iu.getUserId(), iu.getName(), iu.getAge(), iu.getGender(),
                iu.getJob(), iu.getHeight(), iu.getWeight(), iu.getDisease(),
                iu.getResidenceType(), iu.getDiaryReminderEnabled(), iu.getNotificationHour()
        );
    }
}
