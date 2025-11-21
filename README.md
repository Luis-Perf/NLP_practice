# NLP Practice – Classificador de Sentimentos em Java 

Projeto didático de **Processamento de Linguagem Natural (NLP)** em Java, focado em **classificação de sentimento** (positivo / negativo / neutro) em textos curtos em português.

O sistema combina:

- um **classificador por regras** (lexicon-based)
- um **classificador de Machine Learning** usando Tribuo  

Seguindo uma **arquitetura em camadas** (domain / application / infrastructure / presentation) e interface única de domínio para alternar entre as implementações.

> Pensado como projeto de estudos para pós-graduação em IA / NLP e também como portfólio de backend Java.

---

## ✨ Funcionalidades

- Classificação de textos como **POSITIVO, NEGATIVO ou NEUTRO**.
- Dois modos de funcionamento:
  - **Modo 1 – Regras (RuleSentimentClassifier)**  
    Usa um dicionário de palavras positivas/negativas, normalização de texto e tratamento de negação (`"não bom"`, `"não ruim"`, etc.).
  - **Modo 2 – IA / ML (TribuoSentimentClassifier)**  
    Modelo linear treinado com o framework Tribuo em um dataset próprio em português.
- **Fallback inteligente**:  
  quando o modelo de ML não tem informação suficiente (sem features / confiança baixa, se configurado), cai automaticamente para o classificador por regras.
- Dataset simples em arquivos `.txt`, organizado em pastas:
  - `data/train/positivo/`
  - `data/train/negativo/`
  - `data/train/neutro/`

---

## 🧱 Arquitetura

O projeto segue uma estrutura em camadas, separando responsabilidades:

```text
src/main/java/com/example/nlp
├─ domain/           # Regras de negócio / modelo de domínio
│  ├─ SentimentLabel        # Enum POSITIVO / NEGATIVO / NEUTRO
│  ├─ SentimentResult       # Resultado da classificação (label + confiança)
│  └─ SentimentClassifier   # Interface para qualquer classificador de sentimento
│
├─ application/
│  └─ SentimentAnalysisService
│     # Camada de aplicação: usa um SentimentClassifier para analisar textos
│
├─ infrastructure/
│  ├─ RuleSentimentClassifier
│  │   # Classificador baseado em regras (lexicon + tratamento de "nao X")
│  ├─ TribuoSentimentClassifier
│  │   # Classificador baseado em modelo Tribuo (ML) + fallback de regras
│  └─ text/
│      └─ TextPreprocessor
│          # Normaliza o texto (minúsculas, remoção de acentos, espaços)
│
└─ presentation/
   └─ ConsoleApp
      # Interface via linha de comando
```

---

## 🧠 Detalhes de NLP / ML

### Classificador por Regras (`RuleSentimentClassifier`)

- Converte o texto para minúsculas e remove acentos (`ótimo` → `otimo`).
- Quebra em tokens simples.
- Mantém dois conjuntos de palavras:
  - `positiveWords` (bom, otimo, excelente, maravilhoso, amei, recomendo, incrível, etc.)
  - `negativeWords` (ruim, pessimo, horrivel, odiei, lixo, decepcionado, etc.)
- Faz a contagem de ocorrências positivas e negativas.
- Tratamento de negação:
  - `"nao" + palavra positiva` → contribui como **negativo**  
    ex: `nao bom`, `nao otimo`, `nao maravilhoso`
  - `"nao" + palavra negativa` → contribui como **positivo**  
    ex: `nao ruim`, `nao horrivel`, `nao pessimo`

### Classificador de ML (`TribuoSentimentClassifier`)

- Usa **Tribuo** (ML em Java) com:
  - `UniversalTokenizer`
  - `BasicPipeline(tokenizer, 2)` → gera unigramas e bigramas (n-grams até 2)
  - Modelo linear treinado via **SGD + Logistic Regression**.
- Lê os dados de treino de:

  ```text
  data/train/positivo/*.txt
  data/train/negativo/*.txt
  data/train/neutro/*.txt
  ```

  Cada arquivo `.txt` é um exemplo (pode conter 1 ou mais frases coerentes).
- O modelo é salvo em `data/model/tribuo-model.ser` após o treino.
- Em produção:
  - tenta classificar usando o modelo Tribuo;
  - se não encontrar features ou a confiança for muito baixa (threshold, se usado), o texto é encaminhado para o `RuleSentimentClassifier` (fallback).

---

## 🧩 Tecnologias

- **Linguagem:** Java 17  
- **Build:** Maven  
- **ML / NLP:** Tribuo (texto, classificação)  
- **IDE:** IntelliJ IDEA (recomendado)

---

## 🚀 Como executar

### Pré-requisitos

- JDK 17+
- Maven instalado e configurado no `PATH`

### Passos

1. Clonar o repositório:

   ```bash
   git clone https://github.com/SEU-USUARIO/nlp-practice-java.git
   cd nlp-practice-java
   ```

2. (Opcional) Ajustar / revisar os dados em `data/train/...`.

3. Limpar e compilar o projeto:

   ```bash
   mvn clean package
   ```

4. Rodar a aplicação pelo Maven:

   ```bash
   mvn exec:java -Dexec.mainClass="com.example.nlp.presentation.ConsoleApp"
   ```

   ou rodar a classe `ConsoleApp` diretamente pela IDE.

---

## 🖥️ Uso (ConsoleApp)

Ao iniciar, a aplicação pergunta qual modo utilizar:

```text
==== NLP Practice - Sentiment Analyzer ====
Escolha o modo de classificação:
  1 - Regras (RuleSentimentClassifier)
  2 - IA / ML (TribuoSentimentClassifier)
Opção:
```

Depois disso, você pode digitar frases em português:

```text
Texto> O produto é excelente, amei e recomendo!
→ Sentimento: POSITIVO (confiança: 0,61)

Texto> Horrível, me arrependi da compra
→ Sentimento: NEGATIVO (confiança: 0,78)

Texto> O produto é aceitável, nada demais
→ Sentimento: NEUTRO (confiança: 0,50)

Digite 'sair' para encerrar.
```

---

## 📂 Estrutura de dados (treino)

Os exemplos de treino ficam em:

```text
data/
 └─ train/
    ├─ positivo/
    │   ├─ p1.txt
    │   ├─ p2.txt
    │   └─ ...
    ├─ negativo/
    │   ├─ n1.txt
    │   ├─ n2.txt
    │   └─ ...
    └─ neutro/
        ├─ ne1.txt
        ├─ ne2.txt
        └─ ...
```

- Cada arquivo `.txt` = 1 exemplo.
- O conteúdo pode ser uma ou mais frases com o mesmo sentimento.

Para adicionar novos dados:

1. Criar arquivos `.txt` na pasta correspondente (`positivo`, `negativo`, `neutro`).
2. Apagar `data/model/tribuo-model.ser` (para forçar re-treino).
3. Rodar novamente a aplicação em modo IA (opção 2) → o modelo será treinado com a nova base.

---

## 🧪 Possíveis melhorias futuras

- Implementar **testes unitários** (JUnit) para:
  - `RuleSentimentClassifier`
  - `TextPreprocessor`
  - `SentimentAnalysisService`
- Adicionar um módulo de **avaliação automática** (treino/test split) com métricas (accuracy, f1, etc.) em cima dos dados.
- Expor um **endpoint REST** (Spring Boot) em vez de apenas console.
- Persistir feedback do usuário (corrigir classificações) e permitir re-treino incremental.

---

## 📚 Objetivo educacional

Este projeto foi desenvolvido como prática de:

- **NLP em Java** (pré-processamento, n-grams, vocabulário, bag-of-words),
- **Machine Learning supervisionado** com Tribuo,
- **arquitetura limpa** (separação de camadas e interfaces),
- integração de **regras heurísticas + modelo estatístico** num mesmo sistema.

Sinta-se à vontade para clonar, estudar, adaptar e experimentar com novos dados e regras. 
