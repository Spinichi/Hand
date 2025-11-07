import os
import json
from dotenv import load_dotenv
import requests
from langchain_community.vectorstores import Weaviate
from langchain.schema import Document
import weaviate
import asyncio
load_dotenv()

VECTOR_DB = os.getenv("WEAVIATE_URL")
API_KEY = os.getenv("GMS_KEY")
EMB_MODEL = os.getenv("EMBEDDING_MODEL")
EMB_URL = os.getenv("EMBEDDING_GMS_URL")

client = weaviate.Client(VECTOR_DB)

# 🔸 SingleCounsel: 단일 상담 (input + output)
schema_single = {
    "class": "SingleCounsel",
    "description": "단일 상담 사례 데이터",
    "vectorizer": "none",
    "properties": [
        {"name": "input", "dataType": ["text"]},
        {"name": "output", "dataType": ["text"]},
        {"name": "content", "dataType": ["text"]},
        {"name": "tags", "dataType": ["text[]"]},
    ]
}

# 🔸 MultiCounsel: 멀티턴 상담 (대화 흐름)
schema_multi = {
    "class": "MultiCounsel",
    "description": "멀티턴 상담 대화 데이터",
    "vectorizer": "none",
    "properties": [
        {"name": "dialogue", "dataType": ["text"]},
        {"name": "summary", "dataType": ["text"]},
        {"name": "tags", "dataType": ["text[]"]},
    ]
}

for s in [schema_single, schema_multi]:
    try:
        client.schema.create_class(s)
    except:
        print(f"⚠️ 이미 존재: {s['class']}")
        
        

def embed(text:str) -> list:
    headers = {
        "Content-type": "application/json",
        "Authorization": f"Bearer {API_KEY}"
    }
    
    payload = {
        "model": EMB_MODEL,
        "input": text,
    }
    
    response = requests.post(EMB_URL, headers=headers, json=payload)
    response.raise_for_status()
    result = response.json()
    output = result["data"][0]["embedding"]
        
    return output


# ---- SingleCounsel 업로드 ----
with open("./total_kor_counsel_bot.jsonl", "r", encoding="utf-8") as f:
    single_data = json.load(f)

single_objs = []
for s in single_data:
    input_text = s["input"].strip()
    output_text = s["output"].strip()
    combined = f"{input_text}\n{output_text}"
    vector = embed(combined)
    single_objs.append({
        "input": input_text,
        "output": output_text,
        "content": combined,
        "tags": [],
        "_vector": vector
    })

with client.batch as batch:
    batch.batch_size = 10
    for obj in single_objs:
        batch.add_data_object(
            class_name="SingleCounsel",
            data_object=obj,
            vector=obj["_vector"]
        )


# ---- MultiCounsel 업로드 ----
with open("./total_kor_multiturn_counsel_bot.jsonl", "r", encoding="utf-8") as f:
    multi_data = json.load(f)

multi_objs = []
for m in multi_data:
    dialogue = "\n".join([f"{turn['speaker']}: {turn['utterance']}" for turn in m])
    vector = embed(dialogue)
    multi_objs.append({
        "dialogue": dialogue,
        "summary": "",  # 요약은 추후 생성 가능
        "tags": [],
        "_vector": vector
    })

with client.batch as batch:
    batch.batch_size = 10
    for obj in multi_objs:
        batch.add_data_object(
            class_name="MultiCounsel",
            data_object=obj,
            vector=obj["_vector"]
        )

print("✅ 업로드 완료")

