import pandas as pd
import re
from statistics import mean

# Função para extrair métricas de uma string
def extract_metrics(text):
    matches = re.findall(r'Precision: ([\d,]+).*?Recall: ([\d,]+).*?F1 Score: ([\d,]+).*?Jaccard Similarity: ([\d,]+).*?Missing Mandatory Fields: \[(.*?)\].*?Data Type Errors: \[(.*?)\].*?Conformity Rate: ([\d,]+).*?Unknown Rate: ([\d,]+)', text, re.DOTALL)
    result = []
    for match in matches:
        precision, recall, f1, js, mmf, dte, cr, ur = match
        result.append({
            "Precision": float(precision.replace(',', '.')),
            "Recall": float(recall.replace(',', '.')),
            "F1 Score": float(f1.replace(',', '.')),
            "Jaccard Similarity": float(js.replace(',', '.')),
            "MMF": 0 if mmf.strip() == "" else len(mmf.split(',')),
            "DTE": 0 if dte.strip() == "" else len(dte.split(',')),
            "Conformity Rate": float(cr.replace(',', '.')),
            "Unknown Rate": float(ur.replace(',', '.')),
        })
    return result

# Dicionário com todos os modelos e suas métricas (cada string representa um conjunto de métricas por execução)
models_data = {
    "qwen-2.5-max": [
        """
"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"
     """
    ],
    "dolphin-2.9": ["""
        "{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"
"""],
    "gpt-4o": ["""
    "{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 0,9500
Unknown Rate: 0,0500
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"
"""],
    "gemini-2.5-flash": ["""
"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 0,7600
Recall: 0,9500
F1 Score: 0,8444
Jaccard Similarity: 0,7308
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 0,8000
Unknown Rate: 0,2000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 0,8000
Recall: 1,0000
F1 Score: 0,8889
Jaccard Similarity: 0,8000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 0,8000
Unknown Rate: 0,2000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"
                         """],
    "mistral-small-3.1-24b": ["""
"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 0,9500
Unknown Rate: 0,0500
}"	"{Precision: 0,7867
Recall: 0,9200
F1 Score: 0,8000
Jaccard Similarity: 0,7767
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 0,9500
Unknown Rate: 0,0500
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 0,8000
Recall: 1,0000
F1 Score: 0,8889
Jaccard Similarity: 0,8000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 0,9200
Unknown Rate: 0,0800
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 0,9500
Unknown Rate: 0,0500
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"	"{Precision: 1,0000
Recall: 1,0000
F1 Score: 1,0000
Jaccard Similarity: 1,0000
Missing Mandatory Fields: []
Data Type Errors: []
Conformity Rate: 1,0000
Unknown Rate: 0,0000
}"
"""]
}

# calcular AVG - P	AVG - R	AVG - F1 Score	AVG - JS	Total - MMF	Total - DTE	AVG - CR	AVG - UR
def calculate_averages(models_data):
    results = {}
    for model, data in models_data.items():
        all_metrics = []
        for metrics_str in data:
            metrics = extract_metrics(metrics_str)
            all_metrics.extend(metrics)
        
        if all_metrics:
            avg_precision = mean([m["Precision"] for m in all_metrics])
            avg_recall = mean([m["Recall"] for m in all_metrics])
            avg_f1 = mean([m["F1 Score"] for m in all_metrics])
            avg_js = mean([m["Jaccard Similarity"] for m in all_metrics])
            total_mmf = sum(m["MMF"] for m in all_metrics)
            total_dte = sum(m["DTE"] for m in all_metrics)
            avg_cr = mean([m["Conformity Rate"] for m in all_metrics])
            avg_ur = mean([m["Unknown Rate"] for m in all_metrics])

            results[model] = {
                "AVG - P": avg_precision,
                "AVG - R": avg_recall,
                "AVG - F1 Score": avg_f1,
                "AVG - JS": avg_js,
                "Total - MMF": total_mmf,
                "Total - DTE": total_dte,
                "AVG - CR": avg_cr,
                "AVG - UR": avg_ur
            }
    return results

# Executar a função e armazenar os resultados
results = calculate_averages(models_data)
# Criar um DataFrame a partir dos resultados
df = pd.DataFrame.from_dict(results, orient='index')
# Exibir o DataFrame 
print(df)
# Salvar o DataFrame em um arquivo CSV
df.to_csv('model_metrics.csv', index=True)

# Salvar o DataFrame em um arquivo Excel
df.to_excel('model_metrics.xlsx', index=True)
