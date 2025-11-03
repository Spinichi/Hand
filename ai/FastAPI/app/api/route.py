
from fastapi import APIRouter, HTTPException
from app.models.schemas import EmotionOutput, EmotionInput
from app.services.emotion_classify import emotionClassifying
import numpy as np

router = APIRouter()

@router.get("/health", response_model=str)
async def health():
    """서버의 상태를 확인합니다."""
    return "OK"

# 사용자 채팅을 받아 답변 생성
@router.post("/emotion_predict", response_model=EmotionOutput)
async def text_chat(input_data: EmotionInput):
    try:
        user_id = input_data["user_id"]
        text = input_data["text"]
        reply = await emotionClassifying(text)
        result = {"user_id": user_id, "reply": reply}
        return result
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"오류 코드는 {e}")