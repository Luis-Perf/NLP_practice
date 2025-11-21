package com.example.nlp.infrastructure;

import com.example.nlp.domain.SentimentClassifier;
import com.example.nlp.domain.SentimentLabel;
import com.example.nlp.domain.SentimentResult;
import com.example.nlp.infrastructure.text.TextPreprocessor;


import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class RuleSentimentClassifier implements SentimentClassifier {

    private final Set<String> positiveWords = new HashSet<>();
    private final Set<String> negativeWords = new HashSet<>();

    public RuleSentimentClassifier() {
        positiveWords.addAll(Set.of(
                "bom", "boa", "excelente", "otimo", "maravilhoso",
                "gostei", "amei", "perfeito", "satisfeito", "recomendo",
                "incrivel", "adoro", "apaixonado", "fantastico"
        ));

        negativeWords.addAll(Set.of(
                "ruim", "pessimo", "horrivel", "odiei", "terrivel",
                "insatisfeito", "decepcionado", "nao_recomendo", "lixo",
                "péssimo", "odioso", "odio", "fraco"
        ));

    }

    @Override
    public SentimentResult classify(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Texto para classificação não pode ser nulo ou vazio.");
        }

        // Normaliza o texto (minúsculas, sem acento, espaços arrumados)
        String normalized = TextPreprocessor.normalizeBasic(text);
        String[] tokens = normalized.split("\\W+");

        int positiveScore = 0;
        int negativeScore = 0;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];

            if (token.isBlank()) continue;

            // Tratamento especial para "nao {palavra}"
            if (token.equals("nao") && i + 1 < tokens.length) {
                String next = tokens[i + 1];

                // "nao bom", "nao otimo", "nao maravilhoso" vai para NEGATIVO
                if (positiveWords.contains(next)) {
                    negativeScore++;
                    i++; // pulamos o próximo, já usamos
                    continue;
                }

                // "nao ruim", "nao horrivel", "nao pessimo" vai para POSITIVO
                if (negativeWords.contains(next)) {
                    positiveScore++;
                    i++;
                    continue;
                }
                // se for "nao" + palavra neutra, só ignora o "nao" e segue
            }

            // Lógica normal (sem negação)
            if (positiveWords.contains(token)) {
                positiveScore++;
            }
            if (negativeWords.contains(token)) {
                negativeScore++;
            }
        }

        if (positiveScore == 0 && negativeScore == 0) {
            return new SentimentResult(SentimentLabel.NEUTRO, 0.5);
        }

        SentimentLabel label;
        double confidence;

        if (positiveScore > negativeScore) {
            label = SentimentLabel.POSITIVO;
            confidence = (double) positiveScore / (positiveScore + negativeScore);
        } else if (negativeScore > positiveScore) {
            label = SentimentLabel.NEGATIVO;
            confidence = (double) negativeScore / (positiveScore + negativeScore);
        } else {
            label = SentimentLabel.NEUTRO;
            confidence = 0.5;
        }

        return new SentimentResult(label, confidence);
    }

    private String normalize(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "");
    }
}
