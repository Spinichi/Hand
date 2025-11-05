from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api import route
from model_loader import model, tokenizer
from contextlib import asynccontextmanager

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("🚀 서버 시작 중… 모델 Warm-up 중입니다.")
    _ = tokenizer("오늘 해가 나와서 기분 좋아.", return_tensors="pt").to(model.device)
    print("✅ 모델 로드 및 Warm-up 완료")
    
    yield
    
    print("🛑 서버 종료 중… 리소스 정리 완료.")


app = FastAPI(lifespan=lifespan, title="AI Server")

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",
        "https://gatewaytohand.store"
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# 라우터 등록
app.include_router(route.router, prefix="")