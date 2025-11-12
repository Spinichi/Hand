
from fastapi import APIRouter, HTTPException
from app.models.schemas import DiaryOutput, DiaryInput, ManageAdviceInput, ManageAdviceOutput, PersonalAdviceOutput, PersonalAdviceInput, ReportInput, ReportOutput
from app.services.emotion_classify import emotionClassifying
from app.services.report import create_report
from app.services.summary import longSummarize, shortSummarize
from app.services.advice import manager_advice, private_advice
import asyncio

router = APIRouter()

@router.get("/ai-server/health", response_model=str)
async def health():
    """서버의 상태를 확인합니다."""
    return "OK"

# 사용자의 다이어리 문장들을 받아와 오늘의 감정 점수 + 일간 요약(짧은 요약, 긴 요약)을 반환
@router.post("/diary/summary", response_model=DiaryOutput)
async def diary_classification(input_data: DiaryInput):
    try:
        user_id = input_data.user_id
        texts = input_data.texts

        # 텍스트 예외처리
        if not texts:
            raise ValueError("입력된 일기 텍스트가 없습니다.")

        # 감정 분석은 CPU/GPU 연산이므로 별도 스레드로 실행
        classify_task = asyncio.to_thread(emotionClassifying, texts)
        short_task = shortSummarize(" ".join(texts))
        long_task = longSummarize(" ".join(texts))

        classify, short_summary, long_summary = await asyncio.gather(
            classify_task, short_task, long_task
        )

        if "error" in classify:
            raise ValueError(classify["error"])

        result = {
            "user_id": user_id,
            "result": {
                "score": classify["score"],
                "sentiment": classify["sentiment"],
                "short_summary": short_summary,
                "long_summary": long_summary,
            },
        }
        return result
    
    except Exception as e:
        print(f"❌ diary_summary 오류: {e}")
        raise HTTPException(status_code=500, detail=f"오류 코드는 {e}")

# 팀장급에게 1주일치의 보고서와 조언을 제공
@router.post("/manager/advice", response_model = ManageAdviceOutput)
async def group_advice(input_data: ManageAdviceInput):
    try:
        user_id = input_data.user_id
        diaries = input_data.diaries
        biodata = input_data.biometrics
        summaries = input_data.total_summary

        report = create_report(diary=diaries, biodata=biodata)
        advice = manager_advice(report=await report, summary=summaries)

        return ManageAdviceOutput(user_id=user_id, report=report, advice=advice)

    except Exception as e:
            print(f"❌ group_advice 오류: {e}")
            raise HTTPException(status_code=500, detail=f"관리자 조언 생성 중 오류: {e}")

# 개인에게 보고서와 조언 제공(1주일 치)
@router.post("/individual-users/report", response_model = PersonalAdviceOutput)
async def personal_advice(data: PersonalAdviceInput):
    try:
        user_id = data.user_id
        diary = data.diaries
        biodata = data.biometrics
        summaries = data.total_summary

        report = await create_report(diary = diary, biodata = biodata)
        advice = private_advice(report=await report, summary = summaries)

        return PersonalAdviceOutput(user_id=user_id, report=report, advice=advice)

    except Exception as e:
        print(f"❌ personal_advice 오류: {e}")
        raise HTTPException(status_code=500, detail=f"개인 조언 생성 중 오류: {e}")


# # 사용자 다이어리와 점수를 받아와 보고서 작성
# @router.post("/individual-users/daily-report", response_model=ReportOutput)
# async def make_report(data: ReportInput):
#     try:
#         user_id = data.user_id
#         diary = data.diaries
#         biodata = data.biometrics

#         if not diary or not biodata:
#             raise ValueError("다이어리 또는 생체 데이터가 누락되었습니다.")
        
#         report = await create_report(diary = diary, biodata = biodata)

#         return ReportOutput(user_id=user_id, result=report)
    
#     except Exception as e:
#         print(f"❌ individual_report 오류: {e}")
#         raise HTTPException(status_code=500, detail=f"오류 코드는 {e}")