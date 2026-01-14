import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  scenarios: {
    load: {
      executor: "constant-vus",
      vus: 50,
      duration: "10s",
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.01"], // 실패율 1% 미만 목표 (원하면 제거)
  },
};

export default function () {
  // 랜덤 userId (MDC에 들어가게)
  const userId = String(Math.floor(Math.random() * 1000) + 1);

  const url = "http://localhost:8080/api/v1/health";
  const res = http.get(url, {
    headers: { "X-User-Id": userId },
  });

  // traceId 회수
  const traceId = res.headers["X-Trace-Id"];

  // 기본 검증
  check(res, {
    "has traceId header": (r) => !!traceId,
  });

  // 샘플링: 5% 정도만 콘솔에 출력(너무 많이 찍히는 거 방지)
  if (traceId && Math.random() < 0.05) {
    console.log(`sample traceId=${traceId} status=${res.status} userId=${userId}`);
  }

  sleep(0.1);
}
