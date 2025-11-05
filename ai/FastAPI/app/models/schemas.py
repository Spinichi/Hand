from pydantic import BaseModel
from typing import List, Dict

class EmotionInput(BaseModel):
    user_id : int
    texts : List[str]

class EmotionOutput(BaseModel):
    user_id : int
    result: Dict[str, float]
    
class GMSinput(BaseModel):
    text : str
    
class GMSoutput(BaseModel):
    result: str