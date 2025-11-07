import json
import weaviate

client = weaviate.Client("http://localhost:8080")
embed_client = OpenAI(api_key="YOUR_GMS_KEY")  # GPT embedding 3 사용

def embed(text):
    response = embed_client.embeddings.create(
        model="text-embedding-3-small",
        input=text
    )
    return response.data[0].embedding


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
with open(r"C:\Users\SSAFY\Desktop\WANG\CounselGPT\total_kor_multiturn_counsel_bot.jsonl", "r", encoding="utf-8") as f:
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
