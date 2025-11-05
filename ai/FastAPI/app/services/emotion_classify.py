import httpx
from model_loader import model, tokenizer
from transformers import pipeline
import torch
import numpy as np
import re
import emoji
from soynlp.normalizer import repeat_normalize


# 문장을 더 깔끔하게 가공하는 함수

emojis = ''.join(emoji.EMOJI_DATA.keys())
pattern = re.compile(f'[^ .,?!/@$%~％·∼()\x00-\x7Fㄱ-ㅣ가-힣{emojis}]+')
url_pattern = re.compile(
    r'https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)')

def clean(x): 
    x = pattern.sub(' ', x)
    x = emoji.replace_emoji(x, replace='') #emoji 삭제
    x = url_pattern.sub('', x)
    x = x.strip()
    x = repeat_normalize(x, num_repeats=2)
    return x


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
    "기쁨": -1.5,
    "당황": 0.5,
    "분노": 0.8,
    "불안": 0.7,
    "상처": 0.6,
    "슬픔": 1.0
}

tokenizer = tokenizer
model = model

# 감정을 점수화 하는 함수
def emotionClassifying(texts: list[str]) -> dict:
    """
    여러 문장을 받아 감정 평균 확률 + 우울 점수 계산
    """
    all_scores = {label: 0.0 for label in id2label.values()}

    try:
        #  문장별 감정 확률 추출
        for text in texts:
            preds = classifier(text)[0]
            for p in preds:
                all_scores[p["label"]] += p["score"]

        #  평균 확률 계산
        for k in all_scores:
            all_scores[k] = all_scores[k] / len(texts)

        #  가중합 계산 (우울 기여도 반영)
        weighted_sum = sum(all_scores[e] * emotion_weights[e] for e in all_scores)

        #  sigmoid로 0–1 정규화 후 0–100 스케일 변환
        depression_score = 1 / (1 + np.exp(-weighted_sum))
        depression_score = round(depression_score * 100, 2)

        return {
            "sentiment": {k: round(v, 4) for k, v in all_scores.items()},
            "score": depression_score,
        }

    except Exception as e:
        return {"error": f"예측 중 오류 : {str(e)}"}

