// ReliefQueryService.java
package com.finger.hand_backend.relief.service;

import com.finger.hand_backend.relief.entity.Intervention;
import com.finger.hand_backend.relief.repository.InterventionLogRepository;
import com.finger.hand_backend.relief.repository.InterventionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReliefQueryService {

    private final InterventionLogRepository logRepo;
    private final InterventionRepository interventionRepo;

    public List<CopingStatDto> getMyStats(Long userId) {
        List<Object[]> rows = logRepo.statsByUser(userId);
        List<CopingStatDto> out = new ArrayList<>();
        for (Object[] r : rows) {
            Long interventionId = (Long) r[0];
            Long sessions = (Long) r[1];
            Double avgReduction = (Double) r[2];
            Intervention itv = interventionRepo.findById(interventionId).orElse(null);
            out.add(new CopingStatDto(
                    interventionId,
                    itv != null ? itv.getName() : ("#" + interventionId),
                    sessions.intValue(),
                    avgReduction != null ? avgReduction : 0.0
            ));
        }
        return out;
    }

    public record CopingStatDto(Long interventionId, String name, Integer sessions, Double avgReduction) {}
}

