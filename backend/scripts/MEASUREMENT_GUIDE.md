# 다이어리 완료 API 성능 측정 가이드

> **목적**: 트랜잭션 분리 전후 성능 비교를 위한 측정 방법

---

## 사전 준비

### 1. Docker 컨테이너 실행 확인

```bash
# MySQL, MongoDB 컨테이너 상태 확인
docker ps

# 결과 예시:
# hand-mysql-local    -> 3307:3306
# hand-mongodb-local  -> 27018:27017
```

### 2. Spring Boot 애플리케이션 실행

```bash
cd c:\Users\HeeJoon\Desktop\ssafyProject\final\S13P31A106\backend
$env:SPRING_PROFILES_ACTIVE="local"
.\gradlew bootRun
```

애플리케이션이 정상 실행되면 다음 엔드포인트로 확인:
```bash
curl http://localhost:8080/actuator/health
```

### 3. 테스트 데이터 삽입

#### MySQL 데이터 삽입
```bash
# MySQL 컨테이너 접속
docker exec -it hand-mysql-local mysql -u hand_user -phand1234 hand_db

# 스크립트 실행
source /path/to/test-data-mysql.sql
# 또는 Windows에서:
# mysql -h localhost -P 3307 -u hand_user -phand1234 hand_db < backend/scripts/test-data-mysql.sql
```

#### MongoDB 데이터 삽입
```bash
# MongoDB 스크립트 실행
docker exec -i hand-mongodb-local mongosh mongodb://hand_user:hand1234@localhost:27017/hand_db?authSource=admin < backend/scripts/test-data-mongodb.js

# 또는 로컬에서:
mongosh mongodb://hand_user:hand1234@localhost:27018/hand_db?authSource=admin < backend/scripts/test-data-mongodb.js
```

#### 데이터 삽입 확인
**MySQL:**
```sql
SELECT * FROM diary_sessions WHERE user_id = 1;
-- 5개 세션이 IN_PROGRESS 상태로 있어야 함
```

**MongoDB:**
```javascript
use hand_db
db.diary_conversations.find({ userId: NumberLong(1) }).count()
// 5개 문서가 있어야 함
```

---

## Before 측정 (트랜잭션 분리 전)

### 1. Spring Boot 로그 레벨 설정 확인

`application-local.yml`에 다음이 설정되어 있는지 확인:
```yaml
logging:
    level:
        com.finger.hand_backend: DEBUG
        org.hibernate.SQL: DEBUG
        com.zaxxer.hikari.pool.HikariPool: DEBUG
```

### 2. HikariCP 커넥션 풀 모니터링 준비

새 터미널을 열고 다음 명령어로 실시간 모니터링:

```bash
# Active Connections 모니터링 (5초 간격)
while ($true) {
    $time = Get-Date -Format "HH:mm:ss"
    $active = (Invoke-RestMethod -Uri "http://localhost:8080/actuator/metrics/hikaricp.connections.active").measurements[0].value
    $total = (Invoke-RestMethod -Uri "http://localhost:8080/actuator/metrics/hikaricp.connections").measurements[0].value
    Write-Host "[$time] Active: $active / Total: $total"
    Start-Sleep -Seconds 5
}
```

또는 간단하게:
```bash
# 한 번만 확인
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
curl http://localhost:8080/actuator/metrics/hikaricp.connections
curl http://localhost:8080/actuator/metrics/hikaricp.connections.max
```

### 3. 인증 토큰 발급 (필요 시)

다이어리 완료 API는 인증이 필요하므로, 테스트용 JWT 토큰이 필요합니다.

**옵션 1: 로그인 API 호출**
```bash
# 로그인 API 호출
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test1234"}'

# 응답에서 accessToken 복사
```

**옵션 2: 테스트용 인증 비활성화** (권장)
- Spring Security 설정에서 `/api/v1/diaries/**` 경로를 `permitAll()` 설정
- 또는 테스트 프로파일에서 인증 비활성화

### 4. 단순 호출 테스트 (Warm-up)

```bash
# 1회 호출 테스트
curl -X POST http://localhost:8080/api/v1/diaries/1/complete \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# 응답 시간 확인 (Spring Boot 로그에서 확인)
```

### 5. k6 부하 테스트 실행

```bash
cd backend/scripts

# 환경 변수 설정 (필요 시)
$env:BASE_URL = "http://localhost:8080"
$env:AUTH_TOKEN = "Bearer YOUR_TOKEN_HERE"  # 또는 인증 비활성화 시 생략

# k6 실행
k6 run k6-diary-complete-before.js
```

### 6. 측정 데이터 수집

k6 실행 중 다음을 기록:

1. **k6 콘솔 출력**:
   - Total Requests (총 요청 수)
   - Request Rate (RPS)
   - Response Times (avg, p95, p99)
   - Success Rate (성공률)
   - Error Rate (에러율)

2. **HikariCP 메트릭**:
   - Active Connections 최대값
   - Total Connections
   - 커넥션 대기 시간 (있을 경우)

3. **Spring Boot 로그**:
   - 트랜잭션 시작~종료 시간
   - FastAPI 호출 시간 (EmotionAnalysisClient 로그)
   - SQL 실행 로그

4. **결과 파일**:
   - `k6-diary-complete-before-results.json` (자동 생성)

---

## 측정 결과 정리

다음 항목을 표로 정리:

| 지표 | Before (트랜잭션 분리 전) | After (트랜잭션 분리 후) |
|------|---------------------------|--------------------------|
| **API 응답 시간 (평균)** | ? ms | ? ms |
| **API 응답 시간 (p95)** | ? ms | ? ms |
| **API 응답 시간 (p99)** | ? ms | ? ms |
| **처리량 (RPS)** | ? req/s | ? req/s |
| **에러율** | ? % | ? % |
| **HikariCP Active Connections (최대)** | ? / 10 | ? / 10 |
| **트랜잭션 점유 시간** | ? ms | ? ms |
| **FastAPI 호출 시간** | ? ms | ? ms |

---

## After 측정 (트랜잭션 분리 후)

트랜잭션 분리 코드 적용 후:

1. Spring Boot 재시작
2. 테스트 데이터 초기화 (세션 상태를 IN_PROGRESS로 리셋)
3. 동일한 방법으로 k6 실행
4. 결과 비교

---

## 주의사항

1. **FastAPI Mock 서버**: 현재 FastAPI 서버가 없으면 `EmotionAnalysisClient`가 예외를 catch하고 Mock 데이터를 반환합니다. 성능 측정에는 영향이 없지만, 실제 API 호출 시간은 측정되지 않습니다.

2. **데이터 초기화**: 각 테스트 전 세션 상태를 `IN_PROGRESS`로 리셋해야 합니다:
   ```sql
   UPDATE diary_sessions SET status = 'IN_PROGRESS', completed_at = NULL WHERE user_id = 1;
   ```

3. **캐시 영향**: JVM Warm-up을 위해 k6 실행 전 몇 번 수동 호출 권장

4. **동시성 테스트**: 세션 ID 5개를 랜덤하게 선택하므로, 동시에 같은 세션을 완료하려는 경우 낙관적 락 등으로 처리 필요

---

## 트러블슈팅

### 문제: "Connection timeout" 에러
- HikariCP maximum-pool-size가 작을 수 있음 (기본값: 10)
- 트랜잭션 분리 전에는 예상된 현상

### 문제: "Session not found" 에러
- 테스트 데이터가 제대로 삽입되었는지 확인
- 세션 상태가 IN_PROGRESS인지 확인

### 문제: 인증 에러 (401 Unauthorized)
- JWT 토큰이 만료되었거나 잘못되었을 수 있음
- 테스트용 인증 비활성화 권장
