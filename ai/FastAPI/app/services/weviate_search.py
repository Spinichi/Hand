import asyncio
import weaviate
from openai import OpenAI

client = weaviate.Client("http://localhost:8080")
embed_client = OpenAI(api_key="YOUR_GMS_KEY")

def embed(text):
    response = embed_client.embeddings.create(
        model="text-embedding-3-small",
        input=text
    )
    return response.data[0].embedding


def search_weaviate(class_name: str, query_vector: list[float], top_k: int):
    result = (
        client.query
        .get(class_name, ["content", "input", "output", "dialogue", "summary"])
        .with_near_vector({"vector": query_vector})
        .with_limit(top_k)
        .do()
    )
    return result["data"]["Get"][class_name]


async def search_all(query_text: str, top_k: int = 3):
    q_vec = embed(query_text)
    tasks = [
        asyncio.to_thread(search_weaviate, "SingleCounsel", q_vec, top_k),
        asyncio.to_thread(search_weaviate, "MultiCounsel", q_vec, top_k),
    ]
    single_results, multi_results = await asyncio.gather(*tasks)
    return single_results, multi_results


async def build_prompt_for_llm(query_text: str):
    single, multi = await search_all(query_text)

    prompt = f"""
[사용자의 주간 요약]
{query_text}

[유사 단일 상담 예시]
"""
    for s in single:
        prompt += f"- {s['content'][:300]}...\n"

    prompt += "\n[유사 멀티턴 상담 예시]\n"
    for m in multi:
        prompt += f"- {m['dialogue'][:300]}...\n"

    prompt += "\n이러한 사례들을 참고하여 사용자의 상태에 대한 조언을 작성해주세요."
    return prompt
