from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api import route

app = FastAPI()

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "*",
        "http://3.37.250.157:8000",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 라우터 등록
app.include_router(route.router, prefix="")