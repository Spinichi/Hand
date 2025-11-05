
from fastapi import APIRouter, HTTPException
from app.models.schemas import EmotionOutput, EmotionInput, GMSinput, GMSoutput
from app.services.emotion_classify import emotionClassifying
from app.services.report import report
import numpy as np

router = APIRouter()

@router.get("/health", response_model=str)
async def health():
    """서버의 상태를 확인합니다."""
    return "OK"

# 사용자의 다이어리 문장들을 받아 점수화
@router.post("/diary/score", response_model=EmotionOutput)
async def diary_classification(input_data: EmotionInput):
    try:
        user_id = input_data.user_id
        texts = input_data.texts
        
        # 감정 예측
        reply = emotionClassifying(texts)
        result = {"user_id": user_id, "result": reply}
        
        return result
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"오류 코드는 {e}")


# GMS 사용 요청
@router.post("/gms_request", response_model=GMSoutput)
async def gms_request(text: GMSinput):
    try:
        reply = await report(text.text)
        
        return GMSoutput(result = reply)
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"오류 코드는 {e}")