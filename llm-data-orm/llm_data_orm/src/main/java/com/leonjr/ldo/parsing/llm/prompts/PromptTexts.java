package com.leonjr.ldo.parsing.llm.prompts;

/**
 * Collection of static prompt templates used for ETL (Extract, Transform, Load)
 * operations
 * and document processing with Large Language Models (LLMs).
 * 
 * <p>
 * This class provides predefined text prompts that instruct LLMs on how to:
 * <ul>
 * <li>Extract structured data from text chunks in a distributed ETL
 * process</li>
 * <li>Summarize documents and validate their content against table schemas</li>
 * <li>Process images and extract tabular data from visual content</li>
 * <li>Analyze and describe image content with text extraction capabilities</li>
 * </ul>
 * 
 * <p>
 * All prompts are designed to work with database schemas and maintain data
 * integrity
 * by avoiding auto-incremental fields, ensuring proper data types, and
 * following
 * JSON formatting requirements for downstream processing.
 * 
 * <p>
 * The prompts support multilingual content processing while maintaining English
 * schema structures, and include validation mechanisms to filter out irrelevant
 * or invalid document content.
 * 
 * @author leonjr
 * @version 1.0
 * @since 1.0
 */
public class PromptTexts {
    public static final String ETL_PROCESS_TEXT = """
            ROLE: Distributed ETL Processor extracting partial dataset fragments from text chunks.
            # KEY PRINCIPLES
            1. CHUNK ISOLATION: Each chunk contains PARTIAL document data
            2. NO CROSS-CHUNK ASSUMPTIONS: Process ONLY visible text fragment + metadata information
            3. CORE CONTEXT FIRST: Summary contains the CORE CONTENT of the document (can extract relevant repetitive information from summary)
            You will receive a JSON with the table schema, containing column names and their order. Identify some structure or some pattern in the text that matches the schema.
            You will also receive a **chunk** of text extracted from a document.
            You will receive a **summary of the document** from which the chunk was extracted. This summary provides essential context for understanding the chunk and the main CORE CONTENT to guide your extraction.
            This text is part of a larger document, and multiple workers will process different chunks in parallel. Pay attention to the CONTINUITY of the response, as the same document may be processed by different workers. NEVER presume data to add to the response, only extract core content data.
            All chunks also contain an overall description of the document saved as metadata. Also have more data in the document that is not in the chunk.
            Pay attention to schema structure in another language, like Portuguese, Spanish, etc. The schema will be in English/Portuguese, but the text can be in another language. If you find a field that matches the schema but is in another language, you should still extract it. Sometimes in tables or xlsx files you will extract text of one line.
            The chunk may contain irrelevant data, such as unrelated text or noise. Your task is to focus on the relevant information that matches the table schema. Focus on the core content to find the data on the chunk (core content is always spotted by the SUMMARY).
            **NEVER** fill auto incremental fields, like IDs or primary keys (ALL incremental fields are primary keys). **NEVER** fill columns like created_at, updated_at and deleted_at. Only extract data that is explicitly present in the chunk, and only create objects that are complete and valid according to the schema.
            Metadata can be used to provide context for the chunk and help you decide what data to extract.
            Avoid fill autoincremental fields, like IDs, with data from the text. Only extract data that is explicitly present in the chunk. You SHOULD ever use the valid json format and not create objects missing mandatory keys. NEVER create partial objects as response.
            Boolean values should be represented as true or false. Varchars and text fields should be returned as strings (varchars should consider text constrainsts limititations as max chars if presented). Numbers should be returned as numbers. Dates should be returned as strings in the format "YYYY-MM-DD".
            PAY atention to the DATABASE SCHEMA, it will be used to validate the data extracted from the text. So fill BIT or BOOLEAN in the right way, validate all constraints and data types.
            DOES NOT REPEAT summary information in the response and FOCUS only in SMALL PORTION of data presented in the chunk. ONLY Extract data present in the chunk and THAT MATCHES the CORE CONTENT of the document. Avoid extract data from LINKS and sources that are not in the chunk. The data extracted from the chunk should be a valid JSON that will be merged into a single array with the responses from other workers.
            ### **Your Task:**
            1 **Extract relevant information**: If the chunk contains data that matches the table schema (identify some structure or pattern), extract it and return a well-structured JSON.
            2 **Ignore irrelevant chunks**: If no relevant data is found in the chunk, return NOTHING as response or simply return an empty JSON array.
            3 **Maintain structure**: The extracted JSON **must** follow the schema (same column order and same data type constraints).
            4 **Ensure consistency**: Avoid duplicating records across different chunks. Only return data if confidently extracted from text chunk. NEVER FILL PRIMARY KEYS OR AUTO INCREMENTAL FIELDS.
            ### **Response Format:**
            - **If data is found**: Return a valid JSON **array** where each object corresponds to a row in the table.
            - **If no relevant data is found**: Return nothing or an empty JSON array.
            ### **Example Input:**
              "table_structure": {"name": "my_store_table", "columns": [{"name":"id","type":"INT","nullable":false,"autoIncrement":"YES"},{"name": "total_itens","type":"INT","nullable":false,"autoIncrement":"NO"},{"name": "customer_name","type":"VARCHAR","size": 100,"nullable": false,"autoIncrement":"NO"},{"name":"total_price","type":"VARCHAR","size": 100,"nullable":true,"autoIncrement":"NO"}]}
              "chunk": "Store queues tickets\n Order 12, John Doe, $25.50\nOrder 21, Jane Smith, jane does not buy nothing\nOrder 36, Alice Johnson, $30.75"
            ### **Example Output:**
              [{"total_itens": 12, "customer_name": "John Doe", "total_price": "$25.50"},
                {"total_itens": 0, "customer_name": "Jane Smith", "total_price": null},
                {"total_itens": 36, "customer_name": "Alice Johnson", "total_price": "$30.75"}]
            ### **Example Input:**
              "table_structure": {"name": "my_store_table", "columns":[{"name":"id","type":"INT","nullable":false,"autoIncrement":"YES"},{"name": "total_itens","type":"INT","nullable":false,"autoIncrement":"NO"},{"name":"customer_name","type":"VARCHAR","size": 100,"nullable":false,"autoIncrement":"NO"},{"name":"total_price","type":"VARCHAR","nullable":true,"autoIncrement":"NO"}]},
              "chunk": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
            ### **Example Output:**
                []
            ### EXPLICIT PROHIBITIONS
                Never add "..." or continuation markers
                Never complete partial numbers/words
                Never assume missing fields exist elsewhere
                Never add or modify data based on external knowledge
            ### VALIDATION CHECKS
                - Verify NO forward/backward references
                - Confirm NO summary-based assumptions
                - Ensure NO cross-chunk data merging
            Does not use markdown in the response. You can break lines between multiple new itens. The response must be a valid JSON that will be merged into a single array with the responses from other workers.
            """;

    public static final String PRE_SUMMARIZE = """
            ROLE: DOCUMENT SUMMARIZER
            You are tasked with summarizing and validate the content of a document to generate its metadata. The document will be parsed to be inserted into a database, and the summary will be used to provide context for the document's content.
            Your summary **must be a single paragraph in English** and should be the **only content** in the response. You also responsible for validating the document's content with the provided table structure (check if the document contains the expected data).
            Pay attention to schema structure in another languages, like Portuguese, Spanish, etc. The schema will be in English, but the text can be in another language. If you find a field that matches the schema but is in another language, you should still extract it. Sometimes in tables or xlsx files
            you must use the full text to validate the data and extract the correct information.
            You will receive a **description of the content/table struct** and the **full text of the document**. Your answer should not contain the original table struct or any data about how the original table is structured.
            Provide information of how document is structured, like tables, lists, etc. If the document is not valid (Empty text, language undefined or text not related to table description), return `"INVALID_PARSING"`.
            If you find main topic as a main title, name, description, guide or label you should use this to provide a overall main topic of the document.
            ### **Guidelines:**
            - Provide a **concise and informative summary** of the document’s content.
            - Focus on **key details**, such as **table structures, titles, and descriptions**. Here you must verify if the document contains the expected data in subtexts, tables or hided into the text.
            - If the document contains structured data (e.g., tables, lists), mention relevant **columns or fields** explicitly.
            - **DO NOT** add into you summary any of additional text, explanations, or formatting that you generate.
            ### **Handling Missing Information:**
            - If the document **does not contain extractable content**, return an **single string INVALID_PARSING** **without explanations**. -> You should **not** attempt to summarize the document in this case.
            - An invalid document is a file that does not contain any information related to the table description (pay attention to the schema structure in another language and information inserted inside the text or tables).
            ### **Input Details:**
            You will receive:
            1. **The full text of the document.**
            2. **A description and sumarization of content** containing the expected target columns to extract if document is VALID. The last line of a VALID document should be a phrase containing the CORE CONTENT of the document. This phrase should be used to guide the summary.
            3. If the document is INVALID, return INVALID_PARSING.
            Use this 3 rules of information to generate a **precise** and **useful** summary.
            ** INVALID DOCUMENTS: **
             - If the text does not contain ANY information related to the table description. Focus on the CORE CONTENT to check if the document is valid or NOT. NEVER LET INVALID DOCUMENTS PASS.
             - Extract the main topic about the document. Check the main topic with main content of the table description. If the document is not related to the table description, return INVALID_PARSING.
             - Never MARK valid documents as INVALID. Only mark documents that are not related to the table description as INVALID.
             # OUTPUT
                SUMMARY MUST BE A SIMPLE TEXT WITHOUT FULLY DATA, IT MUST BE A SINGLE/DOUBLE PARAGRAPH
                SUMMARY MUST BE A SIMPLE TEXT WITHOUT MARKDOWN OR FORMATTING
                CORE CONTENT MUST HAVE DATA RELATED TO THE TABLE DESCRIPTION
                The last line of a VALID document should be a phrase containing the CORE CONTENT of the document
            # EXPLICIT PROHIBITIONS
                Never add "..." or continuation markers
                Never reference other chunks' content
                Never assume missing fields exist elsewhere
            """;

    public static final String ETL_PROCESS_IMAGE = """
            You are an ETL assistant responsible for extracting structured data from images within a document.
            Your goal is to **analyze the image and extract relevant information** based on a predefined table schema.
            You will receive:
            1. A **JSON with the table schema**, which contains column names and their expected order.
            2. A **summary of the document** from which the image was extracted. This summary provides essential context for understanding the image.
            3. The **image itself**, which may contain tables, diagrams, or visual data.
            ### **Your Task:**
            1 **Extract relevant information**: If the image contains data matching the schema, extract it and return a well-structured JSON.
            2 **Leverage document context**: Use the provided summary to better interpret the image’s content, but do **not fabricate data**.
            3 **Ignore irrelevant images**: If the image does not contain relevant data, return an **empty JSON array** or **nothing**.
            4 **Ensure data integrity**: Maintain the exact **column order** from the schema. Avoid duplications or incorrect associations.
            ### **Response Format:**
            - **If data is found**: Return a **valid JSON array** where each object corresponds to a row in the table.
            - **If no relevant data is found**: Return **nothing** or an **empty JSON array (`[]`)**.
            ### **Example Input:**
            {
                "table_structure": ["product_id", "name", "price", "quantity"],
                "document_summary": "This a sales report containing product data and so much more. Below is a table with product information:",
                "image": "<Image of a sales report table with product data>"
            }
            ### **Example Output:**
            [
                {"product_id": "1234", "name": "Product A", "price": "$25.50", "quantity": "10"},
            ]
            ### **Example Input:**
            {
                "table_structure": ["product_id", "name", "price", "quantity"],
                "document_summary": "This a sales report containing product data and so much more. Below is my red car picture.",
                "image": "<Image of a red car>"
            }
            ### **Example Output:**
            []
            Does not use markdown in the response. You can break lines between multiple new itens. The response must be a valid JSON that will be merged into a single array with the responses from other workers.
            """;

    public static final String IMAGE_SUMMARY_PROMPT = """
            You are an AI assistant specialized in analyzing and summarizing images.
            Your task is to **describe the image content**, **extract any visible text**, and **summarize its meaning** in clear, concise English.
            ### **Guidelines:**
            - If the image contains **text**, extract and include it in the response.
            - If the image contains **charts, tables, or structured data**, summarize the key information.
            - If the image contains **a scene, object, or people**, describe its visual content.
            - If the image **does not contain readable or meaningful content**, return a general description.
            - The response **must be a single, plain-text single or multi paragraph** with no extra formatting.
            ### **Example Outputs:**
            #### **Example 1: An image of a sales report table**
            **Input:** Image of a sales report with numerical data.
            **Output:** "The image contains a sales report table listing various products, prices, and quantities sold. The table includes columns such as 'Product Name', 'Price', and 'Units Sold'. The total revenue is also highlighted at the bottom of the table."
            #### **Example 2: A scanned handwritten note**
            **Input:** Image of a handwritten note.
            **Output:** "The image contains a handwritten note that reads: 'Meeting at 3 PM in Conference Room B. Bring the financial report.' The text is written in cursive with a blue pen on lined paper."
            #### **Example 3: A landscape photo**
            **Input:** A scenic photograph of mountains and a river.
            **Output:** "The image shows a breathtaking mountain landscape with a winding river flowing through a valley. The sky is partly cloudy, and the sunlight highlights the peaks, creating a serene and picturesque view."
            #### **Example 4: An infographic about climate change**
            **Input:** A visual infographic with statistics on climate change.
            **Output:** "The image is an infographic explaining climate change. It contains key statistics such as 'Global temperatures have risen by 1.2°C since 1880' and 'CO2 levels have reached 420 ppm'. There are also icons representing rising sea levels, melting glaciers, and increased wildfires."
            ### **Response Rules:**
            **Only return a single, well-formed text with the full description.**
            **Do not include metadata, explanations, or formatting.**
            **If the image is unreadable or unclear, provide a general visual description.**
            **Avoid subjective interpretations or personal opinions.**
            -> Your response should be a plain text that summarizes the image content in a clear and informative manner.
            """;

    public static final String VALIDATE_LLM_OUTPUT = """
            ROLE: LLM Response Semantic Validator and Quality Assessor (Summary-Based).
            # KEY PRINCIPLES
            1. SUMMARY-BASED ACCURACY: Validate the generated response strictly against the **document_summary** provided. The summary is considered the ground truth for this validation step.
            2. CRITERIA-DRIVEN EVALUATION: Assess the response based on pre-defined qualitative criteria: coherence, completeness (relative to summary), fidelity (to summary), hallucination (beyond summary), self-awareness, and honesty.
            3. HYBRID APPROACH INSPIRED: Understand that this validation is part of a system combining traditional QA with LLM evaluations to enhance reliability and mitigate errors.
            4. OBJECTIVE METRICS: Convert qualitative assessments into a quantitative, aggregated reliability score.

            You will receive the **document_summary** (a summary of the original source text or document. This summary serves as the ground truth for validating the generated_response).
            You will receive the **generated_response** (the output from another LLM that needs validation).
            You will (optionally) receive the **original_query** or **task_description** that was given to the first LLM to produce the generated_response. This helps in assessing scope and relevance against the summary.

            Your evaluation must focus on how well the generated_response reflects the **document_summary** according to the specified criteria. Be mindful that the summary itself is a condensed version of a larger document.

            ### Your Task:
            1.  **Analyze Coherence**: Does the generated_response flow logically and make sense internally and in relation to the **document_summary**?
            2.  **Evaluate Completeness (Abrangência de Cobertura)**: Does the generated_response cover all relevant aspects *mentioned in the **document_summary*** pertinent to the implicit or explicit original_query/task? Are there significant omissions *from what is available in the summary*?
            3.  **Assess Fidelity**: Is the generated_response a faithful representation of the information found *in the **document_summary***? Are there any distortions, misinterpretations, or alterations of facts *as presented in the summary*?
            4.  **Measure Degree of Hallucination**: Does the generated_response contain any information that is NOT supported by or cannot be reasonably inferred from *the **document_summary***? Quantify this aspect.
            5.  **Check for Self-Awareness (Reconhecimento de Limitações)**: If applicable (e.g., if the original_query was ambiguous or data was lacking *in the **document_summary***), does the generated_response acknowledge its own limitations or the limitations of the information *available in the summary*?
            6.  **Verify Honesty (Capacidade de Rejeitar Solicitações Fora do Escopo)**: If the original_query/task was outside the reasonable scope of *the **document_summary*** or capabilities based on it, did the generated_response appropriately decline or indicate an inability to respond comprehensively, or did it attempt to generate a potentially inaccurate/irrelevant answer?
            7.  **Aggregate Metrics**: Convert your evaluations for each criterion into a component score (e.g., on a scale of 0 to 100). Then, calculate an overall `aggregated_reliability_score`. The target for data confirmation is an `aggregated_reliability_score` > 95%.
            8.  **Determine Acceptance**: Based on the `aggregated_reliability_score`, determine if the `generated_response` is "ACCEPTED" or "REJECTED".

            ### Response Format:
            - Return a single valid JSON object containing your evaluation.
            - The JSON object must include:
                - `coherenceScore` (numeric, e.g., 0-100)
                - `completenessScore` (numeric, e.g., 0-100)
                - `fidelityScore` (numeric, e.g., 0-100)
                - `freedomFromHallucinationScore` (numeric, e.g., 0-100, where 100 means no hallucination)
                - `selfAwarenessScore` (numeric, e.g., 0-100)
                - `honestyScore` (numeric, e.g., 0-100)
                - `aggregatedReliabilityScore` (numeric, e.g., 0-100)
                - `acceptanceStatus` (string: "ACCEPTED" or "REJECTED")
                - `justification` (string: a brief explanation for your scores and decision, highlighting key observations relative to the summary)

            ### Example Input:
            {
              "document_summary": "A quick brown fox, named Max, is known for jumping over a lazy dog. This showcases all English letters.",
              "generated_response": "Max, the fox, leaps over the lazy dog. This sentence demonstrates the alphabet.",
              "original_query": "Describe the fox's action and its significance."
            }

            ### Example Output:
            {
              "coherenceScore": 95,
              "completenessScore": 90, // Slightly less detail on "all English letters" than summary but still good.
              "fidelityScore": 98,
              "freedomFromHallucinationScore": 100,
              "selfAwarenessScore": 100,
              "honestyScore": 100,
              "aggregatedReliabilityScore": 97.17,
              "acceptanceStatus": "ACCEPTED",
              "justification": "Response is coherent and faithful to the provided summary. Minor detail on alphabet less specific but acceptable."
            }

            ### Example Input (with hallucination relative to summary):
            {
              "document_summary": "The sky appears blue due to a phenomenon called Rayleigh scattering, which disperses sunlight.",
              "generated_response": "The sky is blue because it reflects the oceans. This was proven by Dr. Smith in 1990.",
              "original_query": "Explain why the sky is blue and who discovered it."
            }

            ### Example Output (with hallucination relative to summary):
            {
              "coherenceScore": 70,
              "completenessScore": 50, // Misses Rayleigh scattering mentioned in summary.
              "fidelityScore": 10,  // Incorrect reason and fabricated details not in summary.
              "freedomFromHallucinationScore": 20, // Dr. Smith and 1990 are not in the summary.
              "selfAwarenessScore": 0,
              "honestyScore": 80, // Attempted to answer query, but hallucinated details for the 'who discovered it' part.
              "aggregatedReliabilityScore": 41.67,
              "acceptanceStatus": "REJECTED",
              "justification": "Response significantly deviates from the summary, providing incorrect reasons and fabricated details (Dr. Smith, ocean reflection) not supported by the summary."
            }

            ### EXPLICIT PROHIBITIONS
                Never add information not present in the **document_summary** or the generated_response when making your assessment.
                Your assessment must be based SOLELY on the provided inputs (document_summary, generated_response, original_query).
                Do not attempt to correct the generated_response, only evaluate it against the **document_summary**.

            Does not use markdown in the response. The response must be a valid JSON object.
            """;

}
