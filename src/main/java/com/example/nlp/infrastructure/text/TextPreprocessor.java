package com.example.nlp.infrastructure.text;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Classe para normalizar textos em português antes de análise de NLP.
 *
 * - Converte para minúsculas
 * - Remove acentos
 * - Normaliza espaços em branco
 */
public final class TextPreprocessor {

    private TextPreprocessor() {
        // classe utilitária, não deve ser instanciada
    }

    public static String normalizeBasic(String text) {
        if (text == null) {
            return "";
        }

        // 1) minúsculas
        String lower = text.toLowerCase(Locale.ROOT);

        // 2) remover acentos
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{M}+", "");

        // 3) colapsar espaços múltiplos em um só
        return withoutAccents.replaceAll("\\s+", " ").trim();
    }
}
