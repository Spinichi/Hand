from dotenv import load_dotenv
import requests
import weaviate
import weaviate.classes as wvc

# Connect to Weaviate
client = weaviate.connect_to_custom(
    http_host="localhost",
    http_port=8080,
    grpc_host="localhost",
    grpc_port=50051,
    http_secure=False,
    grpc_secure=False,
)

single = client.collections.get("SingleCounsel")

result = single.query.fetch_objects(
    limit=1,
    return_vector=True
)

if result.objects:
    obj = result.objects[0]
    print("✅ ID:", obj.id)
    print("✅ input:", obj.properties.get("input"))
    print("✅ output:", obj.properties.get("output"))
    print("✅ vector length:", len(obj.vector) if obj.vector is not None else None)
else:
    print("❌ SingleCounsel 컬렉션에 데이터가 없습니다.")

client.close()