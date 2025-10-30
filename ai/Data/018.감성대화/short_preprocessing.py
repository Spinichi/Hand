import pandas as pd
import os

# 1. 파일 불러오기
path = r"C:\Users\SSAFY\Desktop\WANG\S13P31A106\Data\018.감성대화\Validation_221115_add\원천데이터\감성대화말뭉치(최종데이터)_Validation.xlsx"
df = pd.read_excel(path)

# 2. 주요 컬럼 이름 확인
print('칼럼 이름은', df.columns)

# 3. 필요한 컬럼만 선택 (예시)
cols = [col for col in df.columns if col.startswith("사람문장")] + ["감정_대분류"]

# 4. 결측치 제거
df = df[cols].dropna(subset=['감정_대분류'])
melted = df.melt(id_vars=["감정_대분류"], value_vars=[c for c in df.columns if "사람문장" in c], var_name="문장번호", value_name="text")
melted = melted.dropna(subset=["text"])
melted = melted[melted["text"].str.strip() != ""]
melted = melted.rename(columns={"감정_대분류": "label"})
melted = melted[["text", "label"]].reset_index(drop=True)

# 라벨링
label2id = {label: idx for idx, label in enumerate(sorted(melted["label"].unique()))}
melted["label_id"] = melted["label"].map(label2id)

melted.to_csv("kc_electra_emotion_valid.csv", index=False, encoding="utf-8-sig")

print("✅ 변환 완료!")
print("총 문장 수:", len(melted))
print("라벨 분포:\n", melted["label"].value_counts())
print("라벨 매핑:", label2id)