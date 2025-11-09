# Jenkins AI CI/CD 설정 가이드

## 1. Jenkins 환경변수 설정

Jenkins 관리 > System Configuration > System > Environment variables에 다음 환경변수를 추가하세요:

### 필수 환경변수
```
AI_SERVER_IP=<Server3의 IP 주소>
```

### 기존 환경변수 (이미 설정되어 있어야 함)
```
REGISTRY_PORT=5000
REGISTRY_PRIVATE_IP=<Private Registry IP>
REGISTRY_PUBLIC_IP=<Public Registry IP>
BACKEND_SERVER_IP=<Server2의 IP>
GITLAB_WEBHOOK_TOKEN=<GitLab Webhook Token>
```

---

## 2. Jenkins Credentials 설정

Jenkins 관리 > Credentials > System > Global credentials에 다음 credentials를 추가하세요:

### 2.1 AI 환경변수 파일 (ai-env)
- **ID**: `ai-env`
- **Type**: Secret file
- **File**: `ai/.env` 파일 업로드
- **내용**:
  ```env
  GMS_KEY=<SSAFY GMS API KEY>

  SUMMARY_GMS_URL=https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions
  EMBEDDING_GMS_URL=https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings
  COUNSELING_GMS_URL=https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions

  SHORT_SUMMARY_MODEL=gpt-4.1
  LONG_SUMMARY_MODEL=gpt-4.1
  EMBEDDING_MODEL=text-embedding-3-small
  COUNSELING_MODEL=gpt-4.1

  WEAVIATE_URL=http://localhost:8080
  ```

### 2.2 기존 Credentials (이미 설정되어 있어야 함)
- `gitlab-token`: GitLab access token
- `server-ssh-key`: SSH private key for server access
- `backend-env`: Backend 환경변수 파일
- `firebase_sa_json`: Firebase service account JSON
- `google-services-json`: Google services JSON
- `firebase_app_id_text`: Firebase App ID

---

## 3. Server 3 (AI 서버) 사전 준비

### 3.1 디렉토리 생성
```bash
ssh ubuntu@<AI_SERVER_IP>
mkdir -p /home/ubuntu/ai
```

### 3.2 Docker 및 Docker Compose 설치 확인
```bash
docker --version
docker-compose --version
```

### 3.3 Private Registry 접근 설정
Server 3에서 Private Registry에 접근할 수 있도록 설정:
```bash
# insecure registry 설정 (HTTP registry 사용 시)
sudo vim /etc/docker/daemon.json
```
```json
{
  "insecure-registries": ["<REGISTRY_PUBLIC_IP>:5000"]
}
```
```bash
sudo systemctl restart docker
```

---

## 4. 배포 프로세스

### 4.1 자동 배포 (GitLab Push 시)
1. `ai/` 폴더 내 코드 변경
2. `dev` 브랜치에 push
3. Jenkins가 자동으로 빌드 및 배포 시작

### 4.2 수동 배포
1. Jenkins 프로젝트 페이지에서 "Build with Parameters" 선택
2. `FORCE_BUILD_AI` 체크박스 선택
3. "Build" 버튼 클릭

---

## 5. 배포 구조

### Server 3에서 실행되는 컨테이너
```
hand-ai (FastAPI)
├── Port: 8000
├── Network: ai-network
└── Depends on: hand-weaviate

hand-weaviate (Vector DB)
├── Port: 8080 (내부 네트워크만)
├── Network: ai-network
└── Volume: weaviate_data
```

### 외부 접근 경로
```
사용자 요청
  ↓
https://gatewaytohand.store/ai
  ↓
Nginx (리버스 프록시)
  ↓
Server 3:8000 (FastAPI)
  ↓
Server 3:8080 (Weaviate - 내부 네트워크만)
```

---

## 6. 트러블슈팅

### 6.1 컨테이너가 시작되지 않는 경우
```bash
ssh ubuntu@<AI_SERVER_IP>
cd /home/ubuntu/ai
docker-compose logs
```

### 6.2 Weaviate 연결 실패
```bash
# Weaviate 컨테이너 상태 확인
docker ps | grep weaviate
docker logs hand-weaviate

# Weaviate health check
curl http://localhost:8080/v1/.well-known/ready
```

### 6.3 FastAPI 로그 확인
```bash
docker logs hand-ai
docker logs -f hand-ai  # 실시간 로그
```

### 6.4 이미지 Pull 실패
```bash
# Registry 접근 테스트
docker pull <REGISTRY_PUBLIC_IP>:5000/hand-ai:latest

# Registry 설정 확인
cat /etc/docker/daemon.json
```

---

## 7. 환경변수 설명

### FastAPI 컨테이너 환경변수
- `WEAVIATE_HOST`: Weaviate 서버 호스트 (기본값: weaviate)
- `WEAVIATE_PORT`: Weaviate HTTP 포트 (기본값: 8080)
- `WEAVIATE_GRPC_PORT`: Weaviate gRPC 포트 (기본값: 50051)
- `CORS_ORIGINS`: CORS 허용 도메인 (콤마로 구분)
- `GMS_KEY`: SSAFY GMS API 키
- `*_MODEL`: 사용할 AI 모델명
- `*_GMS_URL`: GMS API 엔드포인트

---

## 8. 참고사항

### 8.1 친구가 작성한 코드 (수정 금지)
- `FastAPI/` 폴더 내 모든 파일
- `vector_db_settings/` 폴더 내 모든 파일
- `requirements.txt`

### 8.2 CI/CD 설정 파일
- `Dockerfile`: FastAPI 이미지 빌드
- `docker-compose.yml`: Weaviate + FastAPI 통합 배포
- `.dockerignore`: 빌드 제외 파일
- `Jenkinsfile`: CI/CD 파이프라인 (프로젝트 루트)

### 8.3 데이터 초기화
Weaviate에 데이터를 넣으려면 Server 3에서:
```bash
cd /home/ubuntu/ai/vector_db_settings
python db_setting.py
```
