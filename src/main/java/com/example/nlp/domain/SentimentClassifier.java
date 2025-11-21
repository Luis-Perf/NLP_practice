package com.example.nlp.domain;

/**
 * Implementações podem usar:
 * - regras simples (palavras-chave),
 * - modelos de Machine Learning,
 * - APIs externas, etc.
 * Mas, para o resto da aplicação, basta saber que:
 * - recebe um texto,
 * - devolve um SentimentResult.
 */
public interface SentimentClassifier {

    /**
     * Classifica o sentimento de um texto.
     *
     * @param text texto de entrada. Não deve ser nulo nem vazio.
     * @return resultado da classificação, nunca nulo.
     * @throws IllegalArgumentException se o texto for nulo ou em branco.
     */
    SentimentResult classify(String text);
}
