/**
 * k6 Race Condition 테스트 스크립트 - 다이어리 완료 동시 요청
 *
 * 목적: 트랜잭션 분리 후, 같은 sessionId에 대해 동시 2건의 완료 요청을 보내서
 *       중복 완료가 실제로 발생하는지 확인
 *
 * 실행 전:
 *   1. reset-test-data.sql 실행 → 세션 status를 IN_PROGRESS로 리셋
 *   2. TARGET_SESSION_ID를 실제로 존재하는 세션 ID로 설정
 *
 * 실행 방법:
 *   k6 run -e TARGET_SESSION_ID=303 k6-race-condition-test.js
 *
 * 판정 기준:
 *   - 둘 다 200 → race condition 발생 (중복 완료)
 *   - 하나만 200, 하나 409/500 → 중복 차단 정상
 */

import http from 'k6/http';
import { check } from 'k6';
import { Rate, Counter } from 'k6/metrics';

const successCount = new Counter('success_200');
const failCount = new Counter('fail_non200');
const raceConditionDetected = new Rate('race_condition_detected');

export const options = {
    vus: 2,
    iterations: 10,
    timeout: '60s',
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TARGET_SESSION_ID = __ENV.TARGET_SESSION_ID || '303';
const AUTH_TOKEN = __ENV.AUTH_TOKEN || 'Bearer test-token-for-user-1';

export default function () {
    const url = `${BASE_URL}/api/v1/diaries/${TARGET_SESSION_ID}/complete`;
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': AUTH_TOKEN,
        },
        timeout: '30s',
    };

    const response = http.post(url, null, params);

    const is200 = response.status === 200;

    check(response, {
        'status recorded': () => true,
    });

    if (is200) {
        successCount.add(1);
    } else {
        failCount.add(1);
    }

    raceConditionDetected.add(is200 ? 1 : 0);
}

export function handleSummary(data) {
    const success = data.metrics.success_200 ? data.metrics.success_200.values.count : 0;
    const fail = data.metrics.fail_non200 ? data.metrics.fail_non200.values.count : 0;
    const total = success + fail;

    let verdict = '';
    if (success > 10) {
        verdict = 'RACE CONDITION DETECTED: ' + success + ' 건이 200 (중복 완료 발생)';
    } else if (success === 10) {
        verdict = 'NORMAL: 정확히 10건 200 (중복 차단 정상)';
    } else {
        verdict = 'UNEXPECTED: ' + success + ' 건 200 (상황 확인 필요)';
    }

    const output = `
================== Race Condition Test ==================
Target Session ID: ${TARGET_SESSION_ID}
Total Requests:    ${total}
  - 200 (완료):    ${success} 건
  - non-200 (거부): ${fail} 건

판정: ${verdict}

참고:
  - 정상(중복 차단): 10건 200 + 10건 에러
  - race condition:  200건수 > 10 (같은 세션이 2번 이상 완료됨)
  - 서버 로그에서 "Phase 3" 로그 확인하여 세션별 완료 횟수 검증 권장
==========================================================
`;

    return {
        'stdout': output,
        'k6-race-condition-results.json': JSON.stringify(data),
    };
}
