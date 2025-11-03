import os
from dotenv import load_dotenv
load_dotenv()

GMS_KEY = os.getenv("GMS_KEY")
GMS_URL = os.getenv("GMS_URL")
MODEL = os.getenv("REPORT_MODEL")

def makeReport(input_data):
    pass
    