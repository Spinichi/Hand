package com.finger.hand_backend.sleep;

import com.finger.hand_backend.sleep.dto.SleepRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Sleep Service
 * - 수면 데이터 저장 및 조회
 */
@Service
@RequiredArgsConstructor
public class SleepService {

    private final SleepRepository sleepRepository;

    /**
     * 수면 데이터 저장
     * - 수면 시작/종료 시간을 받아서 자동으로 수면 시간 계산
     * - 같은 날짜에 이미 데이터가 있으면 업데이트
     *
     * @param userId  사용자 ID
     * @param request 수면 데이터 요청
     * @return 저장된 수면 데이터
     */
    @Transactional
    public Sleep save(Long userId, SleepRequest request) {
        // 검증: 종료 시간이 시작 시간보다 이후여야 함
        if (request.getSleepEndTime().isBefore(request.getSleepStartTime())) {
            throw new IllegalArgumentException("수면 종료 시간은 시작 시간보다 이후여야 합니다");
        }

        // 검증: 미래 시간 불가
        LocalDateTime now = LocalDateTime.now();
        if (request.getSleepStartTime().isAfter(now) || request.getSleepEndTime().isAfter(now)) {
            throw new IllegalArgumentException("수면 시간은 현재 또는 과거여야 합니다");
        }

        // 수면 날짜는 일어난 시간(종료 시간) 기준으로 계산
        LocalDate sleepDate = Sleep.extractSleepDate(request.getSleepEndTime());

        // 같은 날짜의 기존 데이터 확인 (있으면 업데이트, 없으면 새로 생성)
        Optional<Sleep> existingSleep = sleepRepository.findByUserIdAndSleepDate(userId, sleepDate);

        if (existingSleep.isPresent()) {
            // 기존 데이터 업데이트는 현재 JPA에서 지원 안함 -> 삭제 후 재생성
            sleepRepository.delete(existingSleep.get());
        }

        // 수면 시간 계산
        int durationMinutes = Sleep.calculateDurationMinutes(
            request.getSleepStartTime(),
            request.getSleepEndTime()
        );

        // 새로운 수면 데이터 생성
        Sleep sleep = Sleep.builder()
            .userId(userId)
            .sleepStartTime(request.getSleepStartTime())
            .sleepEndTime(request.getSleepEndTime())
            .sleepDurationMinutes(durationMinutes)
            .sleepDate(sleepDate)
            .build();

        return sleepRepository.save(sleep);
    }

    /**
     * 오늘의 수면 데이터 조회
     *
     * @param userId 사용자 ID
     * @return 오늘의 수면 데이터 (없으면 null)
     */
    @Transactional(readOnly = true)
    public Sleep getTodaySleep(Long userId) {
        LocalDate today = LocalDate.now();
        return sleepRepository.findByUserIdAndSleepDate(userId, today).orElse(null);
    }

    /**
     * 특정 날짜의 수면 데이터 조회
     *
     * @param userId    사용자 ID
     * @param sleepDate 수면 날짜
     * @return 수면 데이터 (없으면 null)
     */
    @Transactional(readOnly = true)
    public Sleep getSleepByDate(Long userId, LocalDate sleepDate) {
        return sleepRepository.findByUserIdAndSleepDate(userId, sleepDate).orElse(null);
    }

    /**
     * 사용자의 모든 수면 데이터 조회 (최신순)
     *
     * @param userId 사용자 ID
     * @return 수면 데이터 리스트
     */
    @Transactional(readOnly = true)
    public List<Sleep> getAllSleep(Long userId) {
        return sleepRepository.findByUserIdOrderBySleepDateDesc(userId);
    }

    /**
     * 특정 기간의 수면 데이터 조회
     *
     * @param userId    사용자 ID
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @return 수면 데이터 리스트
     */
    @Transactional(readOnly = true)
    public List<Sleep> getSleepByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return sleepRepository.findByUserIdAndSleepDateBetweenOrderBySleepDateDesc(
            userId, startDate, endDate
        );
    }

    /**
     * 수면 데이터 삭제
     *
     * @param userId  사용자 ID
     * @param sleepId 수면 데이터 ID
     */
    @Transactional
    public void deleteSleep(Long userId, Long sleepId) {
        Sleep sleep = sleepRepository.findById(sleepId)
            .orElseThrow(() -> new IllegalArgumentException("SLEEP_NOT_FOUND"));

        // 본인 데이터인지 확인
        if (!sleep.getUserId().equals(userId)) {
            throw new IllegalArgumentException("NOT_YOUR_SLEEP_DATA");
        }

        sleepRepository.delete(sleep);
    }
}