from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
from datetime import datetime
import time
from g4f.client import Client
import g4f.Provider
import logging

# Set up logging
logging.basicConfig(level=logging.DEBUG)

app = FastAPI()


# OpenAI-like response models
class ChatMessage(BaseModel):
    role: str
    content: object


class ChatResponseChoice(BaseModel):
    index: int
    message: ChatMessage
    finish_reason: str


class ChatResponse(BaseModel):
    id: str
    object: str = "chat.completion"
    created: int
    model: str
    choices: List[ChatResponseChoice]


class ChatRequest(BaseModel):
    model: str
    messages: List[ChatMessage]
    max_tokens: int = 300
    temperature: float = 0.7
    top_p: float = 1.0
    frequency_penalty: float = 0.0
    presence_penalty: float = 0.0
    stop: List[str] = []
    response_format: object = None


@app.post("/v1/chat/completions", response_model=ChatResponse)
def chat_completion(request: ChatRequest):
    try:
        #print the request
        logging.debug(request)
        client = Client()
        # client = g4f.Client(provider=g4f.Provider.Blackbox)
        response = client.chat.completions.create(
            model=request.model,
            messages=[{"role": msg.role, "content": msg.content} for msg in request.messages],
            web_search=False,
            max_tokens=request.max_tokens,
            temperature=request.temperature,
            response_format=request.response_format,
        )

        response_message = response.choices[0].message
        response_dict = {"role": response_message.role, "content": response_message.content}

        return ChatResponse(
            id=f"chatcmpl-{int(time.time() * 1000)}",
            created=int(time.time()),  # Add Unix timestamp
            model=request.model,
            choices=[ChatResponseChoice(index=0, message=ChatMessage(**response_dict), finish_reason="stop")]
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
