import os
import time
import weaviate
import httpx
import json
import re
from fastapi import HTTPException
from dotenv import load_dotenv
from app.core.vector_embedding import embed
from app.services.report import create_report

load_dotenv()

WEAVIATE_URL = os.getenv("WEAVIATE_URL")
ADVICE_URL = os.getenv("COUNSELING_GMS_URL")
ADVICE_MODEL = os.getenv("COUNSELING_MODEL")
GMS_KEY = os.getenv("GMS_KEY")
WEAVIATE_HOST = os.getenv("WEAVIATE_HOST", "localhost")
WEAVIATE_HTTP_PORT = int(os.getenv("WEAVIATE_PORT", "8080"))
WEAVIATE_GRPC_PORT = int(os.getenv("WEAVIATE_GRPC_PORT", "50051"))

print("="*50)
print(f"DEBUG: Attempting to connect to Weaviate with host: '{WEAVIATE_HOST}'")
print("="*50)

# Weaviate 연결 with retry logic
def connect_weaviate_with_retry(max_retries=5, delay=2):
    """Weaviate 연결을 재시도하는 함수"""
    for attempt in range(max_retries):
        try:
            print(f"[WEAVIATE] 연결 시도 {attempt + 1}/{max_retries}...")
            client = weaviate.connect_to_custom(
                http_host=WEAVIATE_HOST,
                http_port=WEAVIATE_HTTP_PORT,
                grpc_host=WEAVIATE_HOST,
                grpc_port=WEAVIATE_GRPC_PORT,
                http_secure=False,
                grpc_secure=False,
            )
            print(f"[WEAVIATE] 연결 성공: {WEAVIATE_HOST}:{WEAVIATE_HTTP_PORT}")
            return client
        except Exception as e:
            print(f"[WEAVIATE] 연결 실패 (시도 {attempt + 1}/{max_retries}): {e}")
            if attempt < max_retries - 1:
                print(f"[WEAVIATE] {delay}초 후 재시도...")
                time.sleep(delay)
            else:
                raise Exception(f"Weaviate 연결 실패: {max_retries}회 시도 후 실패")

client = connect_weaviate_with_retry()

# json 아닌거 터지는 경우 방지
def safe_load_json(text: str):
    """
    LLM 출력에서 JSON 부분만 안전하게 추출해서 Python dict로 변환.
    - ```json ... ``` 제거
    - 설명/문장 제거
    - {} 또는 [] 패턴을 모두 탐지
    - 실패 시 에러 메시지 출력

    Returns:
        dict or list
    """
    try:
        # 1) 코드블록 제거
        text = text.strip()
        text = re.sub(r"```json", "", text, flags=re.IGNORECASE)
        text = re.sub(r"```", "", text)

        # 2) JSON 객체만 정확하게 추출 (중괄호 매칭)
        start = text.find('{')
        if start == -1:
            raise ValueError("JSON 객체를 찾을 수 없습니다")

        # 중괄호 카운팅으로 정확한 끝 찾기
        brace_count = 0
        end = start
        in_string = False
        escape = False

        for i in range(start, len(text)):
            char = text[i]

            # 이스케이프 처리
            if escape:
                escape = False
                continue
            if char == '\\':
                escape = True
                continue

            # 문자열 내부 체크
            if char == '"':
                in_string = not in_string
                continue

            # 문자열 밖에서만 중괄호 카운트
            if not in_string:
                if char == '{':
                    brace_count += 1
                elif char == '}':
                    brace_count -= 1
                    if brace_count == 0:
                        end = i + 1
                        break

        json_str = text[start:end]
        return json.loads(json_str)

    except Exception as e:
        print("❌ JSON 파싱 실패:", e)
        print("원본 텍스트 (처음 500자):\n", text[:500] if len(text) > 500 else text)
        raise e

# rerank를 더 잘 이해하게 하기 위해
def list_to_bullet(items: list):
    if not items:
        return "- 없음"
    return "\n".join([f"- {str(i).strip()}" for i in items])

async def rerank(summary: str, single_retrieval: list, multi_retrieval:list):
    prompt = f"""
        당신은 감정 상담 및 정신건강 조언에 특화된 전문가 시스템입니다.
        아래는 사용자의 현재 심리 상태를 요약한 내용입니다:

        [사용자 요약]
        {summary}

        아래는 RAG 시스템이 벡터 기반으로 검색한 상담 기록 후보들입니다.
        이제 이 후보들을 기반으로 **사용자에게 가장 적합한 조언 근거 데이터**만 걸러내고 재정렬해야 합니다.

        [싱글턴 상담 데이터]
        {list_to_bullet(single_retrieval)}

        [멀티턴 상담 데이터]
        {list_to_bullet(multi_retrieval)}

        ---
        Rerank 목표

        당신의 역할은 아래 기준을 바탕으로 **싱글턴+멀티턴 상담 데이터를 통합하여**
        사용자에게 도움이 될 가능성이 높은 순서대로 재랭킹하는 것입니다.

        ### 평가 기준
        1. **내용 관련성(Relevance)**
        - 요약된 사용자 감정 상태와 얼마나 직접적으로 연결되는가?

        2. **문제 구조 유사성(Situation Similarity)**
        - 상황(관계, 스트레스 요인, 감정 패턴)이 얼마나 닮았는가?

        3. **감정적 유사성(Emotional Matching)**
        - 감정적 맥락(불안/분노/슬픔/상처 등)이 일치하는가?

        4. **조언 가능성(Helpfulness Potential)**
        - 해당 상담사례가 실제로 조언 생성에 도움이 될 수 있는가?

        5. **중복 제거(Deduplication)**
        - 의미가 겹치거나 비슷한 사례는 묶어서 점수는 낮게.

        ---

        ## 출력 형식 (JSON)
        아래 형식을 반드시 지켜주세요:

        {{
        "ranked_items": [
            {{
            "type": "single" | "multi",
            "content": "원문 상담 내용"
            }}
        ],
        "top_k_final": [
            "상위 3개의 상담 내용만 원문 그대로"
        ]
        }}

        주의:
        - score는 0~1 실수
        - 최대 3개(top_k=3)를 최종 리턴
        - 사용자의 심리와 무관한 데이터는 score를 낮게 책정

        ---

        ## 🎯 최종 작업
        주어진 데이터 중 **가장 관련성 높은 상담 사례 3개만** 선별하여
        JSON 형식으로 rerank 결과를 출력하세요.
        """

    headers = {
    "Content-Type": "application/json",
    "Authorization": f"Bearer {GMS_KEY}",
    }

    messages = [
        {
            "role": "system",
            "content": "당신은 vector_db에서 추출한 내용을 rerank 하는 평가자입니다.",
        },
        {
            "role": "user",
            "content": prompt,
        },
    ]

    payload = {
        "model": "gpt-4.1-nano",
        "messages": messages,
        "max_tokens": 3000,
        "temperature": 0.3,
    }

    try:
        async with httpx.AsyncClient(verify=False, timeout=30.0) as cli:
            response = await cli.post(ADVICE_URL, headers=headers, json=payload)
            response.raise_for_status()
            result = response.json()

        advice = result["choices"][0]["message"]["content"].strip()

        return advice

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"GMS 요청 중 오류 발생: {e}")

# 유사 상담내용 검색
async def retrieve_similar_cases(query: str, info, top_k: int = 5):
    try:
        prompt = f"""
        {query}
        사용자 정보
        나이 : {info.age}
        직업 : {info.job}
        질병력 : {info.disease}
        성별 : {info.gender}
        거주 형태 : {info.family}
        """
        # 쿼리 임베딩 생성
        query_vector = embed(prompt)
        
        # 뭔가 오류가 터지는데 뭔지 몰라서 찍어보는 것.
        if query_vector is None or not isinstance(query_vector, list):
            raise ValueError("Embedding 함수가 벡터를 반환하지 않았습니다.")

        # 단일 상담 검색
        single_col = client.collections.get("SingleCounsel")
        single_res = single_col.query.hybrid(
            query=prompt,
            vector=query_vector,
            alpha=0.5,
            limit=top_k,
            return_properties=["output"],
        )

        # 멀티턴 상담 검색
        multi_coll = client.collections.get("MultiCounsel")
        multi_res = multi_coll.query.hybrid(
            query=prompt,
            vector=query_vector,
            alpha = 0.5,
            limit=top_k,
            return_properties=["counselor"],
        )

        # 결과만 텍스트로 추출
        single_texts = [o.properties.get("output", "") for o in single_res.objects]
        multi_texts = [o.properties.get("counselor", "") for o in multi_res.objects]

        return single_texts or [], multi_texts or []

    except Exception as e:
        print(f"❌ 상담 검색 중 오류: {e}")
        return [], []

# 관리자 조언 생성 함수
async def manager_advice(report: str, summary: str, info: dict):
    single, multi = await retrieve_similar_cases(summary, info)

    # 리랭크 실행
    rerank_result = await rerank(summary, single, multi)
    rerank_data = safe_load_json(rerank_result)

    top3 = rerank_data.get("top_k_final", [])
    if not top3:
        reranked_text = "\n".join(single) if single else "유사 상담 데이터를 찾지 못했습니다."
    else:
        # 리랭크 된 애들을 합쳐서 하나의 텍스트로 변환
        top3 = rerank_data["top_k_final"]
        reranked_text = "\n".join(top3)
    
    prompt = f"""
        당신은 팀장으로서 팀원의 상태를 보고 조언을 제시하는 역할입니다.
        - 팀장만 할 수 있는 조언을 위주로 작성할 것. 개인에게도 추천할 수 있는 방법보다는 관리자 입장에서의 조언을 만들어야 함.
        - 존댓말로 조언 작성
        - 불필요한 감정 표현은 피하고, 현실적이고 따뜻하게 조언할 것
        - 팀장은 상담 전문가가 아니므로 보다 안전하고 조심스러운 접근 방법을 제시할 것.
        - 유사한 상담 사례를 참고할 것.
        - 답변은 최소 300자, 최대 500자를 넘기지 말것.
        
        [팀원의 일주일치 상태 보고서]
        {report}

        [팀원의 상태와 유사한 사람과의 상담 사례]
        {reranked_text}
        
        답변 생성 시 유사한 상담의 예시를 모두 참고하세요. 만약 유사 상담이 없을 경우 알아서 조언을 생성해주세요.
        아래의 형식을 참고하여 비슷한 형태로 생성하되, 아래의 형식의 내용은 참고하지 마세요.
        제안은 최대 3개까지만 제공해주세요.
        상태 요약을 짧고 간략하게 핵심만 뽑아주세요.

        상태 요약 : 요즘 화재 출동이 많아지면서 스트레스가 누적되고, 수면 부족까지 겹쳐 많이 힘드실 것 같습니다. 누구라도 이런 상황이 지속되면 집중력이 떨어질 수밖에 없습니다.
        현재 본인의 상태를 스스로 인지하고 계신 것은 정말 중요한 부분이라고 생각합니다. 업무 특성상 긴장 상태가 길게 이어지면 몸과 마음 모두 지치기 쉽기  때문에, 작은 변화라도 시도해보는 것이 필요합니다.

        이런 제안을 해주는건 어떨까요?

        제안:
        1. 짧은 휴식이라도 챙기기
        바쁜 와중에도 잠깐이라도 눈을 감고 숨을 고르거나, 스트레칭을 해보시길 권합니다. 짧은 시간이더라도 반복적으로 휴식을 취하면 몸이 조금은 회복하는 데 도움이 될 수 있습니다.

        2. 수면 환경 점검하기
        퇴근 후에는 가급적 전자기기 사용을 줄이고, 밝은 조명을 피하는 등 잠자기 좋은 환경을 만들어보세요. 잠이 부족하면 업무 집중력에 더 큰 영향을 줄 수 있으니, 수면 시간을 조금이라도 확보하는 것이 중요합니다.

        3. 주변에 도움 요청하기
        혼자서 모든 부담을 안으려고 하지 않으셨으면 합니다. 팀 내에서 업무 분담이 조정이 가능한 부분이 있다면 꼭 말씀해주셔도 좋고, 서로 힘든 부분을 공유하는 것만으로도 심리적으로 도움이 될 수 있습니다.
        """
        
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {GMS_KEY}",
    }
    
    messages = [
        {
            "role": "system",
            "content": "당신은 정서적으로 불안정한 팀원에게 상담을 해줘야 하는 팀장에게 가이드라인을 제시하는 상담 코치입니다. 한국어로 대답해 주세요. 관리자만이 할 수 있는 조언 위주로 답변을 만들어주세요.",
        },
        {
            "role": "user",
            "content": prompt,
        },
    ]

    payload = {
        "model": ADVICE_MODEL,
        "messages": messages,
        "max_tokens": 500,
        "temperature": 0.6,
    }

    try:
        async with httpx.AsyncClient(verify=False, timeout=30.0) as cli:
            response = await cli.post(ADVICE_URL, headers=headers, json=payload)
            response.raise_for_status()
            result = response.json()
            
        advice = result["choices"][0]["message"]["content"].strip()
        
        return advice

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"GMS 요청 중 오류 발생: {e}")
    


# 개인용 조언 생성 함수
async def private_advice(report: str, summary: str, info: dict):
    single, multi = await retrieve_similar_cases(summary, info=info)

    # 리랭크 실행
    rerank_result = await rerank(summary, single, multi)
    rerank_data = safe_load_json(rerank_result)

    top3 = rerank_data.get("top_k_final", [])
    if not top3:
        reranked_text = "\n".join(single) if single else "유사 상담 데이터를 찾지 못했습니다."
    else:
        # 리랭크 된 애들을 합쳐서 하나의 텍스트로 변환
        top3 = rerank_data["top_k_final"]
        reranked_text = "\n".join(top3)
    
    prompt = f"""
        당신은 정서적으로 불안정할 수 있는 사람에게 작은 조언을 주는 역할입니다.
        - 존댓말로 조언 작성
        - 불필요한 감정 표현은 피하고, 현실적이고 따뜻하게 조언할 것
        - 당신은 상담 전문가가 아니므로 보다 안전하고 조심스러운 접근 방법을 제시할 것.
        - 유사한 상담 사례를 참고할 것.
        - 답변은 최소 100자, 최대 300자를 넘기지 말것.

        [사용자의 일주일치 다이어리 보고서]
        {report}

        [사용자의 상태와 유사한 사람과의 상담 사례]
        {reranked_text}
        
        답변 생성 시 위의 실제 상담 사례를 모두 참고하세요.
        아래의 형식을 참고하여 비슷한 형태로 생성하되, 아래의 형식의 내용은 참고하지 마세요.
        제안은 최대 3개까지만 짧게 제공해주세요.

        제안:
        1. 짧은 휴식이라도 챙기기
        바쁜 와중에도 잠깐이라도 눈을 감고 숨을 고르거나, 스트레칭을 해보시길 권합니다. 짧은 시간이더라도 반복적으로 휴식을 취하면 몸이 조금은 회복하는 데 도움이 될 수 있습니다.

        2. 수면 환경 점검하기
        퇴근 후에는 가급적 전자기기 사용을 줄이고, 밝은 조명을 피하는 등 잠자기 좋은 환경을 만들어보세요. 잠이 부족하면 업무 집중력에 더 큰 영향을 줄 수 있으니, 수면 시간을 조금이라도 확보하는 것이 중요합니다.

        3. 주변에 도움 요청하기
        혼자서 모든 부담을 안으려고 하지 않으셨으면 합니다. 팀 내에서 업무 분담이 조정이 가능한 부분이 있다면 꼭 말씀해주셔도 좋고, 서로 힘든 부분을 공유하는 것만으로도 심리적으로 도움이 될 수 있습니다.
        """
        
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {GMS_KEY}",
    }
    
    messages = [
        {
            "role": "system",
            "content": "당신은 정서적으로 불안정한 팀원에게 상담을 해주는 코치입니다. 한국어로 대답해 주세요.",
        },
        {
            "role": "user",
            "content": prompt,
        },
    ]

    payload = {
        "model": ADVICE_MODEL,
        "messages": messages,
        "max_tokens": 500,
        "temperature": 0.6,
    }

    try:
        async with httpx.AsyncClient(verify=False, timeout=20.0) as cli:
            response = await cli.post(ADVICE_URL, headers=headers, json=payload)
            response.raise_for_status()
            result = response.json()
            
        advice = result["choices"][0]["message"]["content"].strip()
        return advice

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"GMS 요청 중 오류 발생: {e}")

# 개인용 조언 생성 함수
async def daily_advice(text: str):
    prompt = f"""
        당신은 정서적으로 불안정할 수 있는 사람에게 매우 짧은 조언을 주는 역할입니다. 아래의 조건을 참고하세요.

        [조건]
        - 존댓말로 조언 작성
        - 불필요한 감정 표현은 피하고, 현실적이고 따뜻하게 조언할 것
        - 당신은 상담 전문가가 아니므로 보다 안전하고 조심스러운 접근 방법을 제시할 것.
        - 유사한 상담 사례를 참고할 것.
        - 답변은 아래의 예시를 참고하되, 각 조언 당 50글자를 넘지 않을 것.


        [예시]

        예시 다이어리 내용 : 오늘 회사를 다녀오는 길에 어떤 사람이 술에 취해서 시비를 걸었어. 너무 불쾌한데 어쩔 수 없다는게 화나. 계속 머릿속에 맴돌아서 고통스러워.

        [출력]

        오늘 술에 취한 사람 때문에 기분이 좋지 않으시군요. 이렇게 해보는건 어떠신가요?

        조언 1 : 가볍게 산책하며 머리를 비우기.
        조언 2 : 따듯하고 맛있는 음식 먹으며 소소한 행복 찾기.

        [실제 사용자의 다이어리]
        {text}

        """

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {GMS_KEY}",
    }
    messages = [
        {
            "role": "system",
            "content": "당신은 정서적으로 불안정한 사용자에게 조언을 해주는 친구입니다. 한국어로 대답해 주세요.",
        },
        {
            "role": "user",
            "content": prompt,
        },
    ]

    payload = {
        "model": ADVICE_MODEL,
        "messages": messages,
        "max_tokens": 200,
        "temperature": 0.6,
    }

    try:
        async with httpx.AsyncClient(verify=False, timeout=20.0) as cli:
            response = await cli.post(ADVICE_URL, headers=headers, json=payload)
            response.raise_for_status()
            result = response.json()

        advice = result["choices"][0]["message"]["content"].strip()
        return advice

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"GMS 요청 중 오류 발생: {e}")
