from pydantic import BaseModel
from typing import List, Dict

class EmotionInput(BaseModel):
    input_data : dict

class EmotionOutput(BaseModel):
    result: dict