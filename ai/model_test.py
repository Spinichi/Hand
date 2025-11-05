from transformers import (
    AutoTokenizer,
    AutoModelForSequenceClassification,
)
import torch
import torch.nn.functional as F

model_path = r"C:\Users\SSAFY\Desktop\WANG\S13P31A106\ai\Classifier_Model"
model = AutoModelForSequenceClassification.from_pretrained(model_path)
tokenizer = AutoTokenizer.from_pretrained(model_path)

from transformers import pipeline
import torch
import numpy as np

# 감정 라벨 매핑
id2label = {
    0: "기쁨",       # happy
    1: "당황",       # embarrass
    2: "분노",       # anger
    3: "불안",       # unrest
    4: "상처",       # damaged
    5: "슬픔"        # sadness
}
label2id = {v: k for k, v in id2label.items()}

# id2label 정보를 config에 반영
model.config.id2label = id2label
model.config.label2id = label2id

# 파이프라인 정의
classifier = pipeline(
    "text-classification",
    model=model,
    tokenizer=tokenizer,
    return_all_scores=True,
    device=0 if torch.cuda.is_available() else -1
)

# 감정별 가중치 (심리 영향 기반)
# → 값이 높을수록 우울/불안에 기여도가 큼
emotion_weights = {
    "기쁨": -1.0,
    "당황": 0.5,
    "분노": 0.8,
    "불안": 0.6,
    "상처": 0.8,
    "슬픔": 1.0
}

def emotionClassifying(texts: list[str]) -> dict:
    """
    여러 문장을 받아 감정 평균 확률 + 우울 점수 계산
    """
    all_scores = {label: 0.0 for label in id2label.values()}

    try:
        # 1️⃣ 문장별 감정 확률 추출
        for text in texts:
            preds = classifier(text)[0]
            print(f"\n대상 문장: {text}")
            print("감정 확률:")
            
            for p in preds:
                all_scores[p["label"]] += p["score"]
                print(f"  - {p['label']:>3}: {p['score']:.4f}")
                

        # 2️⃣ 평균 확률 계산
        for k in all_scores:
            all_scores[k] = all_scores[k] / len(texts)

        # 3️⃣ 가중합 계산 (우울 기여도 반영)
        weighted_sum = sum(all_scores[e] * emotion_weights[e] for e in all_scores)

        # 4️⃣ sigmoid로 0–1 정규화 후 0–100 스케일 변환
        depression_score = 1 / (1 + np.exp(-weighted_sum))
        depression_score = round(depression_score * 100, 2)

        # 5️⃣ 요약 메시지
        if depression_score < 40:
            level = "안정"
            msg = "오늘은 비교적 안정된 감정 상태입니다."
        elif depression_score < 60:
            level = "주의"
            msg = "약간의 스트레스나 불안이 감지됩니다."
        elif depression_score < 80:
            level = "위험"
            msg = "우울 신호가 뚜렷합니다. 휴식이 필요해요."
        else:
            level = "심각"
            msg = "심리적 위험 상태로 보입니다. 전문가 상담을 권장합니다."

        return {
            "평균감정확률": {k: round(v, 4) for k, v in all_scores.items()},
            "우울점수(0-100)": depression_score,
            "상태등급": level,
            "요약": msg
        }

    except Exception as e:
        return {"error": f"예측 중 오류 발생: {str(e)}"}


# 테스트
texts = [
    "오늘은 너무 지쳤다. 회사에서 혼나서 기분이 우울했다.",
    "집에 와서도 아무 의욕이 없다.",
    "내일은 조금 나아질까 기대해본다."
]
print(emotionClassifying(texts))
