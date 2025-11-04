package com.finger.hand_backend.baseline;

import com.finger.hand_backend.baseline.dto.BaselineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Baseline Controller
 * - 사용자별 개인화된 스트레스 기준값 관리
 */
@RestController
@RequestMapping("/baselines")
@RequiredArgsConstructor
public class BaselineController {

    private final BaselineService baselineService;

    /**
     * Baseline 계산 및 생성
     * - 최근 N일간 편안한 상태(stress_level ≤ 2) 측정 데이터로부터 계산
     * - 기존 활성 Baseline 비활성화하고 새 버전 생성
     *
     * @param authentication 인증 정보
     * @param days           수집 기간 (기본 3일)
     * @return 생성된 Baseline
     */
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateBaseline(
            Authentication authentication,
            @RequestParam(defaultValue = "3") Integer days
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Baseline baseline = baselineService.calculateAndSave(userId, days);

        Map<String, Object> response = new HashMap<>();
        response.put("baseline", BaselineResponse.from(baseline));
        response.put("message", "Baseline이 생성되었습니다");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 활성 Baseline 조회
     *
     * @param authentication 인증 정보
     * @return 활성 Baseline
     */
    @GetMapping("/active")
    public ResponseEntity<BaselineResponse> getActiveBaseline(
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Baseline baseline = baselineService.getActiveBaseline(userId);

        return ResponseEntity.ok(BaselineResponse.from(baseline));
    }

    /**
     * Baseline 이력 조회 (페이징)
     *
     * @param authentication 인증 정보
     * @param pageable       페이징 정보 (기본: 10개, 최신순)
     * @return Baseline 페이지
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getBaselineHistory(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "version", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Page<Baseline> baselinePage = baselineService.getBaselineHistory(userId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("total", baselinePage.getTotalElements());
        response.put("page", baselinePage.getNumber());
        response.put("size", baselinePage.getSize());
        response.put("baselines", baselinePage.getContent().stream()
                .map(BaselineResponse::from)
                .toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 버전 Baseline 조회
     *
     * @param authentication 인증 정보
     * @param version        버전 번호
     * @return Baseline
     */
    @GetMapping("/{version}")
    public ResponseEntity<BaselineResponse> getBaselineByVersion(
            Authentication authentication,
            @PathVariable Integer version
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Baseline baseline = baselineService.getBaselineByVersion(userId, version);

        return ResponseEntity.ok(BaselineResponse.from(baseline));
    }

    /**
     * Baseline 업데이트 (재계산)
     * - 새 데이터로 Baseline 재계산
     * - 새 버전으로 생성하고 활성화
     *
     * @param authentication 인증 정보
     * @param days           수집 기간 (기본 3일)
     * @return 업데이트된 Baseline
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateBaseline(
            Authentication authentication,
            @RequestParam(defaultValue = "3") Integer days
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Baseline baseline = baselineService.updateBaseline(userId, days);

        Map<String, Object> response = new HashMap<>();
        response.put("baseline", BaselineResponse.from(baseline));
        response.put("message", "Baseline이 업데이트되었습니다");

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 버전 Baseline 활성화
     * - 기존 활성 Baseline 비활성화
     * - 지정한 버전 활성화
     *
     * @param authentication 인증 정보
     * @param version        활성화할 버전
     * @return 활성화된 Baseline
     */
    @PutMapping("/{version}/activate")
    public ResponseEntity<Map<String, Object>> activateBaseline(
            Authentication authentication,
            @PathVariable Integer version
    ) {
        Long userId = Long.valueOf(authentication.getName());

        Baseline baseline = baselineService.activateBaseline(userId, version);

        Map<String, Object> response = new HashMap<>();
        response.put("baseline", BaselineResponse.from(baseline));
        response.put("message", "Baseline이 활성화되었습니다");

        return ResponseEntity.ok(response);
    }

    /**
     * Baseline 삭제
     * - 활성 Baseline은 삭제 불가
     *
     * @param authentication 인증 정보
     * @param version        삭제할 버전
     * @return 204 No Content
     */
    @DeleteMapping("/{version}")
    public ResponseEntity<Void> deleteBaseline(
            Authentication authentication,
            @PathVariable Integer version
    ) {
        Long userId = Long.valueOf(authentication.getName());

        baselineService.deleteBaseline(userId, version);

        return ResponseEntity.noContent().build();
    }
}
