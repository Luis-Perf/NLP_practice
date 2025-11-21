package com.example.nlp.application;

import com.example.nlp.domain.SentimentClassifier;
import com.example.nlp.domain.SentimentResult;

/**
 * Camada de aplicação responsável por orquestrar o fluxo de análise de sentimento.
 * Ela:
 * - recebe um texto da camada de apresentação
 * - delega a classificação para um SentimentClassifier (domínio/infra);
 * - devolve um SentimentResult para quem chamou.
 *
 * Não conhece detalhes de implementação (regras, ML, API externa),
 * apenas depende da 'interface' SentimentClassifier.
 */
public class SentimentAnalysisService {

    private final SentimentClassifier classifier;

    public SentimentAnalysisService(SentimentClassifier classifier) {
        if (classifier == null) {
            throw new IllegalArgumentException("SentimentClassifier não pode ser nulo.");
        }
        this.classifier = classifier;
    }

    /**
     * Analisa o texto recebido e retorna o resultado de sentimento.
     *
     * Regras de validação adicionais ('log', auditoria, métricas, etc.)
     * podem ser adicionadas aqui no futuro.
     */
    public SentimentResult analyze(String text) {
        // Aqui poderíamos centralizar validações de negócio, 'logs', etc.
        return classifier.classify(text);
    }
}
