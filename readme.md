# HAND - Have A Nice Day 👋

> 갤럭시 워치 기반 24시간 실시간 심리 모니터링 시스템

**팀명**: FINGER
**프로젝트 기간**: 2025.10 ~ 2025.11

---

## 📌 프로젝트 소개

HAND는 갤럭시 워치를 활용하여 사용자의 심리 상태를 24시간 모니터링하고, 실시간으로 이상 징후를 감지하여 적절한 완화법을 제안하는 서비스입니다.

### 핵심 기능
- 🔍 **실시간 이상치 감지**
- 📊 **매일 자정 슬픔 지수 계산** (0-100점)
- 💆 **완화법 제안** (심호흡, 그라운딩, 근육이완)
- 👥 **그룹 관리** (관리자 대시보드)

---

## 🛠️ 기술 스택

### Backend
- Java 21
- Spring Boot 3.x
- Spring Cloud Gateway
- Gradle
- MySQL, MongoDB, Redis
- RabbitMQ

### Frontend
- Android (Kotlin)
- Wear OS (Galaxy Watch)

### AI
- Python, FastAPI
- Weaviate (Vector DB)

### DevOps
- Jenkins
- Docker
- Nginx
- Prometheus + Grafana

---

## 🔀 브랜치 전략 (GitHub Flow)

우리 팀은 **GitHub Flow**를 사용합니다.

### 브랜치 구조
```
master (배포 브랜치)
  ├── S13P11A106-31
  ├── S13P11A106-45
  └── S13P11A106-52
```

### 작업 흐름
1. **이슈 생성** (Jira)
2. **브랜치 생성** 
   ```bash
   git checkout -b S13P11A106-31
   ```
3. **작업 & 커밋**
4. **Pull Request 생성** → `master`으로
5. **Merge** → `master` 브랜치에 병합
6. **자동 배포** (Jenkins CI/CD)

### 브랜치 네이밍 규칙
```
[이슈번호]
```

**예시**:
- `S13P11A106-31`
- `S13P11A106-52`

---

## 📝 커밋 컨벤션

### 기본 형식
```
<type>: <subject> [이슈번호]
```

### Commit Types
| Type | 설명 |
|------|------|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 |
| `style` | 코드 포맷팅, 세미콜론 누락 등 (코드 변경 없음) |
| `refactor` | 코드 리팩토링 |
| `chore` | 빌드 업무, 패키지 매니저 수정 (.gitignore 등) |

### 예시
```bash
feat: 초기 프로젝트 프론트엔드 구조 설정 [S13P11A106-31]

fix: 로그인 시 토큰 만료 에러 수정 [S13P11A106-52]

docs: README에 브랜치 전략 추가 [S13P11A106-15]

refactor: 생체 데이터 처리 로직 개선 [S13P11A106-88]
```

### 상세 커밋 컨벤션
더 자세한 내용은 [노션 문서](https://www.notion.so/28b0a418a18c8173901dc294ea1e13a3)를 참고하세요.

---

## 📏 네이밍 컨벤션

### Java/Kotlin
- **변수**: `camelCase`
  ```java
  String userName = "홍길동";
  int heartRate = 75;
  ```

- **상수**: `UPPER_SNAKE_CASE`
  ```java
  public static final int MAX_HEART_RATE = 200;
  public static final String API_BASE_URL = "https://api.hand.com";
  ```

- **함수명**: 동사 + 명사
  ```java
  // Good
  getUserInfo()
  calculateRiskScore()
  sendNotification()
  
  // Bad
  user()
  riskScore()
  notification()
  ```

- **클래스명**: `PascalCase`
  ```java
  public class BiometricDataService { }
  public class AnomalyDetector { }
  ```

---

## 🔧 개발 환경 설정

### Tool 선택
1. **API 명세**: Swagger 3.0
2. **Build Tool**: Gradle
3. **설정 파일**: `application.yml` 사용

### 환경 변수 예시
```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  data:
    mongodb:
      uri: ${MONGO_URI}
```

---

## 📁 프로젝트 구조

```
S13P31A106/
├── backend/
│   ├── business/           # 서버2 (Main Backend)
│   └── dataprocessing/     # 서버3 (Data Processing)
├── frontend/
│   ├── mobile/             # Android 앱
│   └── watch/              # Wear OS 앱
├── ai/                     # 서버4 (AI & Monitoring)
├── nginx/                  # 서버1 (Gateway)
└── infra/
    ├── monitoring/
    └── scripts/
```

---

## 🚀 시작하기

### 1. 저장소 클론
```bash
git clone [repository-url]
cd S13P31A106
```

### 2. 환경 변수 설정
```bash
cp .env.example .env
# .env 파일 편집
```

### 3. Docker Compose 실행
```bash
docker-compose up -d
```

**© 2025 FINGER Team. All rights reserved.**
