from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Dict
import json
import sys
from g4f.client import Client

app = FastAPI()

class ChatMessage(BaseModel):
    role: str
    content: str

class ChatRequest(BaseModel):
    model: str
    messages: List[ChatMessage]
    temperature: float = 0.7

class ChatResponseChoice(BaseModel):
    message: ChatMessage

class ChatResponse(BaseModel):
    choices: List[ChatResponseChoice]

@app.post("/v1/chat/completions", response_model=ChatResponse)
def chat_completion(request: ChatRequest):
    try:
        client = Client()
        response = client.chat.completions.create(
            model=request.model,
            messages=[{"role": msg.role, "content": msg.content} for msg in request.messages],
            web_search=False
        )

        response_message = response.choices[0].message  # This is a ChatCompletionMessage object
        response_dict = {"role": response_message.role, "content": response_message.content}  # Convert to dict
        
        return ChatResponse(choices=[ChatResponseChoice(message=ChatMessage(**response_dict))])
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
