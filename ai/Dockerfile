# Python 3.10 slim 이미지 사용
FROM python:3.10-slim

# 작업 디렉토리 설정
WORKDIR /app

# 시스템 의존성 설치 (torch, opencv 등을 위한 필수 라이브러리)
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    libgomp1 \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender-dev \
    libgl1 \
    && rm -rf /var/lib/apt/lists/*

# FastAPI 실행에 필요한 최소 패키지만 설치
RUN pip install --no-cache-dir \
    fastapi==0.120.0 \
    uvicorn==0.38.0 \
    starlette==0.48.0 \
    pydantic==2.11.2 \
    pydantic_core==2.33.1 \
    python-dotenv==1.1.0 \
    torch==2.5.1 \
    transformers==4.57.1 \
    tokenizers==0.22.1 \
    safetensors==0.6.2 \
    huggingface-hub==0.35.3 \
    weaviate-client==4.17.0 \
    langchain==1.0.4 \
    langchain-core==1.0.3 \
    langchain-community \
    openai==1.70.0 \
    httpx==0.28.1 \
    requests==2.32.4 \
    numpy==1.26.4 \
    soynlp==0.0.493 \
    emoji==2.15.0

# FastAPI 애플리케이션 코드 복사
COPY FastAPI/ ./FastAPI/

# 작업 디렉토리를 FastAPI로 이동
WORKDIR /app/FastAPI

# 포트 노출
EXPOSE 8000

# FastAPI 실행
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
