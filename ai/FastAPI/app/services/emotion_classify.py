import httpx
import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification, pipeline

Model_Path = r"C:\Users\SSAFY\Desktop\WANG\S13P31A106\ai\Classifier_Model"
tokenizer = AutoTokenizer.from_pretrained(Model_Path, use_fast=False)
model = AutoModelForSequenceClassification.from_pretrained(Model_Path)

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

# id2label 정보를 직접 설정 (config에 추가)
model.config.id2label = id2label
model.config.label2id = label2id

classifier = pipeline(
    "text-classification",
    model=model,
    tokenizer=tokenizer,
    return_all_scores=True,
    device=0 if torch.cuda.is_available() else -1
)

def emotionClassifying(texts: list[str]) -> dict[str, float]:
    # 여러 문장을 받아 평균 감정 확률 계산
    all_scores = {label: 0.0 for label in id2label.values()}


    try:
        for text in texts:
            preds = classifier(text)[0]  # pipeline 결과
            for p in preds:
                all_scores[p["label"]] += p["score"]

        # 평균 확률 계산
        for k in all_scores:
            all_scores[k] = round(all_scores[k] / len(texts), 4)

        return all_scores

    except Exception as e:
        # 추론 중 에러 시 안전하게 반환
        return {"error": f"예측 중 오류 발생: {str(e)}"}
