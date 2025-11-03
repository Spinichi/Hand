import httpx
import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification, pipeline

Model_Path = r"C:\Users\SSAFY\Desktop\WANG\S13P31A106\ai\Classifier_Model"
tokenizer = AutoTokenizer.from_pretrained(Model_Path)
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

def emotionClassifying(text):
    classifier = pipeline(
        "text-classification",
        model=model,
        tokenizer=tokenizer,
        return_all_scores=True,
        device=0 if torch.cuda.is_available() else -1
    )
    
    result = classifier(text)[0]
    result = sorted(result, key=lambda x: x["score"], reverse=True)
    
    return {"result" : result}
