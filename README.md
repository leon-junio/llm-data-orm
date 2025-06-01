# LLM DATA ORM

A crescente necessidade de migração eficiente de dados não estruturados para sistemas modernos de banco de dados impulsiona a criação de soluções mais automatizadas e precisas. Este trabalho tem como objetivo desenvolver uma biblioteca em Java no formato ORM (Mapeamento Objeto-Relacional) para realizar a migração de dados não estruturados para dados estruturados em bancos de dados relacionais.

## Requisitos

- JDK 17 ou Java SE 17 instalado na máquina. [Baixar JDK 17](https://www.oracle.com/br/java/technologies/downloads/#jdk17-windows)

## Execução

Para compilar o código e buildar a aplicação execute o seguinte em um terminal
```bash
  # Na pasta Root do programa
   cd src 
  .\mvnw clean
  .\mvnw compile
  .\mvnw build
  .\mvnw package
``` 

## Modelos selecionados para teste
- `dolphin-2.9` – Modelo de código aberto baseado no `LLaMA` ([https://huggingface.co/cognitivecomputations/dolphin-2.9-llama3-8b](https://huggingface.co/cognitivecomputations/dolphin-2.9-llama3-8b));
- `qwen-2.5-max` – Modelo de uso livre treinado pela `Alibaba` ([https://qwenlm.github.io/blog/qwen2.5-max/](https://qwenlm.github.io/blog/qwen2.5-max/));
- `gpt-4o` – Modelo de uso restrito treinado pela `OpenAI` ([https://explodingtopics.com/blog/gpt-parameters](https://explodingtopics.com/blog/gpt-parameters));
- `gemini-2.5-flash` – Modelo restrito treinado pela `Google` ([https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-5-flash](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-5-flash));
- `mistral-small-3.1-24b` – Modelo de uso livre treinado pela `Mistral` ([https://huggingface.co/mistralai/Mistral-Small-3.1-24B-Instruct-2503](https://huggingface.co/mistralai/Mistral-Small-3.1-24B-Instruct-2503)).

## Autores

- [Leon](https://www.github.com/leon-junio)
