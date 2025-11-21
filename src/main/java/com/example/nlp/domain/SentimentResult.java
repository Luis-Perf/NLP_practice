package com.example.nlp.domain;

/**
 * Resultado da classificação de sentimento de um texto.
 */
public class SentimentResult {

    private final SentimentLabel label;
    private final double confidence; // valor entre 0.0 e 1.0

    public SentimentResult(SentimentLabel label, double confidence) {
        if (label == null) {
            throw new IllegalArgumentException("Label de sentimento não pode ser nulo.");
        }
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("Confiança deve estar entre 0.0 e 1.0.");
        }
        this.label = label;
        this.confidence = confidence;
    }

    public SentimentLabel getLabel() {
        return label;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return "SentimentResult{" +
                "label=" + label +
                ", confidence=" + confidence +
                '}';
    }
}
