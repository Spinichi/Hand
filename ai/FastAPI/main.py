from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api import route
from model_loader import session, tokenizer
from contextlib import asynccontextmanager
import os

@asynccontextmanager
async def lifespan(app: FastAPI):
    try:
        print("🚀 서버 시작 중… ONNX 모델 Warm-up 중입니다.")

        # 토크나이저 입력 준비 (ONNX는 numpy 입력 사용)
        inputs = tokenizer("오늘 해가 나와서 기분 좋아.", return_tensors="np")

        # ONNX Runtime으로 warm-up 실행
        ort_inputs = {
            "input_ids": inputs["input_ids"],
            "attention_mask": inputs["attention_mask"]
        }
        _ = session.run(["logits"], ort_inputs)

        print("✅ ONNX 모델 로드 및 Warm-up 완료")

    except Exception as e:
        print(f"❌ Warm-up 실패: {e}")

    # FastAPI 앱이 실행되는 동안 유지
    yield

    # 서버 종료 시 리소스 정리
    print("🛑 서버 종료 중…")


app = FastAPI(lifespan=lifespan, title="AI Server")

# CORS 설정
allowed_origins = os.getenv("CORS_ORIGINS", "https://gatewaytohand.store").split(",")
app.add_middleware(
    CORSMiddleware,
    allow_origins=allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# 라우터 등록
app.include_router(route.router, prefix="/ai")