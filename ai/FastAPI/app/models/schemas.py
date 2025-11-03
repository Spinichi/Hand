from pydantic import BaseModel
from typing import List, Dict

class EmotionInput(BaseModel):
    user_id : int
    text : List[str]

class EmotionOutput(BaseModel):
    user_id : int
    result: Dict[str, float]