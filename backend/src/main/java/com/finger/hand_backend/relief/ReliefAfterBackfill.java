package com.finger.hand_backend.relief;

import com.finger.hand_backend.measurement.Measurement;
import com.finger.hand_backend.measurement.MeasurementRepository;
import com.finger.hand_backend.relief.entity.InterventionLog;
import com.finger.hand_backend.relief.repository.InterventionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReliefAfterBackfill {

    private final InterventionLogRepository logRepo;

    @Value("${relief.match.post-window-minutes:5}")
    private int postWindowMinutes;

    /**
     * Measurement가 새로 저장된 직후 호출해 주세요.
     * 조건: 해당 사용자에 대해 endedAt이 있고 afterStress가 아직 NULL이며,
     *       endedAt < m.measuredAt <= endedAt + window 인 가장 최신 세션 1건
     */
    @Transactional
    public void onNewMeasurement(Long userId, Measurement m) {
        LocalDateTime t = m.getMeasuredAt();

        Optional<InterventionLog> target = logRepo
                .findTop1ByUserIdAndAfterStressIsNullAndEndedAtIsNotNullOrderByEndedAtDesc(userId)
                .filter(log -> {
                    var end = log.getEndedAt();
                    if (end == null) return false;

                    // ★ 변경 전: t.isAfter(end) && !t.isAfter(end.plusMinutes(postWindowMinutes))
                    // ★ 변경 후: t >= end && t <= end + window
                    return !t.isBefore(end) &&            // t >= end
                            !t.isAfter(end.plusMinutes(postWindowMinutes)); // t <= end+window
                });

        target.ifPresent(log -> {
            Double stressIndex = m.getStressIndex();
            log.setAfterStress(stressIndex != null ? stressIndex.intValue() : null);
        });
    }
}
