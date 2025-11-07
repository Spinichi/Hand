import os
import httpx
import asyncio
from dotenv import load_dotenv
from fastapi import HTTPException

load_dotenv()

API_KEY = os.getenv("GMS_KEY")
SHORT_SUMMARY_MODEL = os.getenv("SHORT_SUMMARY_MODEL")
LONG_SUMMARY_MODEL = os.getenv("LONG_SUMMARY_MODEL")
SUMMARY_URL = os.getenv("SUMMARY_GMS_URL")

print(f"키값은 : {API_KEY}")