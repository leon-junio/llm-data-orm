# LLM DATA ORM

A crescente necessidade de migração eficiente de dados não estruturados para sistemas modernos de banco de dados impulsiona a criação de soluções mais automatizadas e precisas. Este trabalho tem como objetivo desenvolver uma biblioteca em Java para realizar a migração de dados não estruturados para dados estruturados em bancos de dados relacionais.

---

## Requisitos

- JDK 17 ou Java SE 17 instalado na máquina. [Baixar JDK 17](https://www.oracle.com/br/java/technologies/downloads/#jdk17-windows)

---

## Sobre o Código

O projeto `llm-data-orm` é uma aplicação Java desenvolvida para realizar a migração de dados não estruturados para bancos de dados relacionais. A estrutura do projeto é organizada da seguinte forma:

-   **`src/main/java/com/leonjr/ldo`**: Contém o código fonte principal da aplicação.
    -   **`Main.java`**: Ponto de entrada da aplicação. Utiliza a biblioteca PicoCLI para criar uma interface de linha de comando (CLI) robusta, permitindo a configuração e execução do pipeline ETL.
    -   **`ETLPipeline.java`**: Orquestra o processo de Extração, Transformação e Carga dos dados.
    -   **`AppStore.java`**: Gerencia o estado e as configurações da aplicação durante a execução.
    -   **`app/`**: Pacote contendo sub-pacotes para:
        -   `consts`: Constantes da aplicação.
        -   `enums`: Enumerações utilizadas no projeto.
        -   `helper`: Classes utilitárias (ex: `LoggerHelper`, `YmlHelper`).
        -   `models`: Modelos de dados ou entidades.
    -   **`database/`**: Responsável pela interação com o banco de dados (ex: `DBHelper`).
    -   **`extractor/`**: Componentes para extração de dados de diversas fontes.
    -   **`parsing/`**: Classes para análise e interpretação dos dados extraídos.
    -   **`validation/`**: Lógica para validação dos dados.
-   **`src/main/resources`**: Arquivos de configuração e outros recursos.
-   **`pom.xml`**: Arquivo de configuração do Maven, gerenciando as dependências e o build do projeto.

A classe `Main.java` é o coração da interface da aplicação, definindo os parâmetros que podem ser passados via linha de comando para controlar o comportamento do ORM de dados. Ela inicializa as configurações, o banco de dados e, se instruído, inicia o pipeline ETL.

---

## Compilação e Empacotamento

Para compilar o código e gerar o arquivo `.jar` executável, siga os passos abaixo. Assumindo que você está na pasta raiz do projeto:

1.  **Navegue até a pasta `src` (se seus comandos `mvnw` estão lá, senão, execute da raiz onde `mvnw` está):**
    ```bash
    # Se mvnw está na raiz, pule este passo. 
    # Se os comandos abaixo devem ser executados de dentro de src, use:
    # cd src 
    ```
    *Nota: Normalmente, os comandos `mvnw` são executados a partir da pasta raiz do projeto (onde o arquivo `pom.xml` e o próprio `mvnw` estão localizados).*

2.  **Limpe o projeto (opcional, mas recomendado para um build limpo):**
    ```bash
    ./mvnw clean 
    ```
    *No Windows, você pode precisar usar `.\mvnw.cmd clean` ou `mvnw.cmd clean`.*

3.  **Compile o código:**
    ```bash
    ./mvnw compile
    ```

4.  **Empacote a aplicação (gera o JAR):**
    ```bash
    ./mvnw package
    ```
    Este comando criará um arquivo JAR na pasta `target/`. O nome do arquivo geralmente segue o padrão `<artifactId>-<version>.jar` (ex: `ldo-1.0.jar` ou similar, dependendo da configuração no `pom.xml`).

---

## Execução

Após compilar e empacotar a aplicação, você pode executá-la usando o arquivo JAR gerado. Abra um terminal na pasta raiz do projeto e utilize o seguinte comando:

```bash
java -jar target/llm_data_orm-1.0.jar [OPÇÕES]
````

Substitua `llm_data_orm-1.0.jar` pelo nome real do arquivo JAR gerado na pasta `target` (por exemplo, `llm_data_orm-1.0.jar`).

### Opções de Linha de Comando:

A aplicação aceita os seguintes argumentos de linha de comando:

  - `-c, --config <configFilePath>`: **(Obrigatório)** Caminho para o arquivo de configuração.
  - `-t, --table <tableName>`: **(Obrigatório)** Nome da tabela para recuperar informações.
  - `-f, --file, --folder <fileOrFolderpath>`: Caminho para o arquivo ou pasta a ser processado.
  - `-e, --exec`: Executa o processo ETL. Se esta flag não for fornecida, a aplicação apenas inicializa (boot), mas não processa os dados.
  - `-d, --debug`: Habilita o modo de depuração (debug).
  - `-ts, --testset <testSetPath>`: Caminho para o conjunto de testes.
  - `-p, --pages <pagesRegex>`: Regex para processar páginas específicas (se aplicável). Pode ser usado para representar um conjunto de páginas (ex: `1,2,3`) ou um intervalo (ex: `1-3`). Se não definido, todas as páginas serão processadas.
  - `-h, --help`: Exibe a mensagem de ajuda com todas as opções disponíveis.

### Exemplos de Execução:

1.  **Exibir a ajuda:**

    ```bash
    java -jar target/ldo-1.0.jar -h
    ```

2.  **Executar o pipeline ETL para um arquivo específico:**

    ```bash
    java -jar target/ldo-1.0.jar -c /caminho/para/config.yml -t nome_da_tabela -f /caminho/para/seu_arquivo.pdf -e
    ```

3.  **Executar em modo debug para uma pasta, processando páginas específicas:**

    ```bash
    java -jar target/ldo-1.0.jar -c conf/settings.yml -t minha_tabela -f ./documentos_entrada/ -e -d -p "1-5,8,10"
    ```

-----

## Modelos Selecionados para Teste

  - `dolphin-2.9` – Modelo de código aberto baseado no `LLaMA` ([https://huggingface.co/cognitivecomputations/dolphin-2.9-llama3-8b](https://huggingface.co/cognitivecomputations/dolphin-2.9-llama3-8b));
  - `qwen-2.5-max` – Modelo de uso livre treinado pela `Alibaba` ([https://qwenlm.github.io/blog/qwen2.5-max/](https://qwenlm.github.io/blog/qwen2.5-max/));
  - `gpt-4o` – Modelo de uso restrito treinado pela `OpenAI` ([https://explodingtopics.com/blog/gpt-parameters](https://explodingtopics.com/blog/gpt-parameters));
  - `gemini-2.5-flash` – Modelo restrito treinado pela `Google` ([https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-5-flash](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-5-flash));
  - `mistral-small-3.1-24b` – Modelo de uso livre treinado pela `Mistral` ([https://huggingface.co/mistralai/Mistral-Small-3.1-24B-Instruct-2503](https://huggingface.co/mistralai/Mistral-Small-3.1-24B-Instruct-2503)).

-----

## Autores

  - [Leon](https://www.github.com/leon-junio)