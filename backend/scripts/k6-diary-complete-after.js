/**
 * k6 부하 테스트 스크립트 - 다이어리 완료 API (After - 트랜잭션 분리 후)
 *
 * 목적: 트랜잭션 분리 후 성능 측정
 * - API 응답 시간
 * - 처리량 (RPS)
 * - 에러율
 *
 * 실행 방법:
 * k6 run k6-diary-complete-after.js
 *
 * 주의: 100개 세션 데이터 필요 (test-data-mysql-100sessions.sql 먼저 실행)
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const diaryCompleteTime = new Trend('diary_complete_duration');

// 테스트 설정
export const options = {
    // 시나리오 1: 점진적 부하 증가 (Ramp-up)
    stages: [
        { duration: '30s', target: 5 },   // 30초 동안 5명까지 증가
        { duration: '1m', target: 10 },   // 1분 동안 10명 유지
        { duration: '30s', target: 20 },  // 30초 동안 20명까지 증가
        { duration: '1m', target: 20 },   // 1분 동안 20명 유지
        { duration: '30s', target: 0 },   // 30초 동안 종료
    ],
    thresholds: {
        // 성공률 95% 이상
        'checks': ['rate>0.95'],
        // 평균 응답 시간 10초 이하
        'http_req_duration': ['p(95)<10000'],
        // 에러율 5% 미만
        'errors': ['rate<0.05'],
    },
};

// 테스트 데이터
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// 100개 세션 ID 생성 (MySQL auto_increment 기준)
// 실제 환경에서는 setup()에서 DB 조회해서 가져오는 게 좋지만, 여기선 단순화
const MIN_SESSION_ID = 303;
const MAX_SESSION_ID = 402;

// 간단한 JWT 토큰 생성 (실제로는 로그인 API로 발급받아야 함)
// 테스트용으로 사전에 발급받은 토큰을 사용하거나, 인증을 임시로 비활성화해야 함
const AUTH_TOKEN = __ENV.AUTH_TOKEN || 'Bearer test-token-for-user-1';

export default function () {
    // 1~100 사이 랜덤 세션 ID 선택
    // const sessionId = Math.floor(Math.random() * (MAX_SESSION_ID - MIN_SESSION_ID + 1)) + MIN_SESSION_ID;
    const COUNT = MAX_SESSION_ID - MIN_SESSION_ID + 1; // 100
    const sessionId = MIN_SESSION_ID + (__ITER % COUNT);
    const url = `${BASE_URL}/api/v1/diaries/${sessionId}/complete`;
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': AUTH_TOKEN,
        },
        timeout: '30s',  // 타임아웃 30초
    };

    // API 호출 시작 시간
    const start = Date.now();

    const response = http.post(url, null, params);

    // API 호출 종료 시간
    const duration = Date.now() - start;
    diaryCompleteTime.add(duration);

    // 응답 검증
    const success = check(response, {
        'status is 200': (r) => r.status === 200,
        'response has data': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data !== null && body.data !== undefined;
            } catch (e) {
                return false;
            }
        },
        'response time < 10s': () => duration < 10000,
    });

    // 에러율 기록
    errorRate.add(!success);

    // 요청 간 대기 시간 (1~3초)
    sleep(Math.random() * 2 + 1);
}

// 테스트 종료 후 요약
export function handleSummary(data) {
    return {
        'stdout': textSummary(data, { indent: ' ', enableColors: true }),
        'k6-diary-complete-after-results.json': JSON.stringify(data),
    };
}

function textSummary(data, options) {
    const indent = options.indent || '';
    const enableColors = options.enableColors || false;

    return `
${indent}================== Test Summary ==================
${indent}
${indent}Total Requests:     ${data.metrics.http_reqs.values.count}
${indent}Request Rate:       ${data.metrics.http_reqs.values.rate.toFixed(2)} req/s
${indent}
${indent}Response Times:
${indent}  - avg:            ${data.metrics.http_req_duration.values.avg.toFixed(2)} ms
${indent}  - min:            ${data.metrics.http_req_duration.values.min.toFixed(2)} ms
${indent}  - max:            ${data.metrics.http_req_duration.values.max.toFixed(2)} ms
${indent}  - p(95):          ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)} ms
${indent}  - p(99):          ${data.metrics.http_req_duration.values['p(99)'].toFixed(2)} ms
${indent}
${indent}Success Rate:       ${(data.metrics.checks.values.rate * 100).toFixed(2)}%
${indent}Error Rate:         ${(data.metrics.errors.values.rate * 100).toFixed(2)}%
${indent}
${indent}Diary Complete API:
${indent}  - avg:            ${data.metrics.diary_complete_duration.values.avg.toFixed(2)} ms
${indent}  - p(95):          ${data.metrics.diary_complete_duration.values['p(95)'].toFixed(2)} ms
${indent}
${indent}==================================================
`;
}
