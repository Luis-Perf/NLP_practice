package com.example.nlp.presentation;

import com.example.nlp.application.SentimentAnalysisService;
import com.example.nlp.domain.SentimentClassifier;
import com.example.nlp.domain.SentimentResult;
import com.example.nlp.infrastructure.RuleSentimentClassifier;
import com.example.nlp.infrastructure.TribuoSentimentClassifier;

import java.util.Scanner;

/**
 * Camada de apresentação via console.
 *
 * Permite escolher entre:
 * - classificador por REGRAS (RuleSentimentClassifier)
 * - classificador por IA / ML (TribuoSentimentClassifier)
 */
public class ConsoleApp {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            System.out.println("==== NLP Practice - Sentiment Analyzer ====");
            System.out.println("Escolha o modo de classificaç2ão:");
            System.out.println("  1 - Regras (RuleSentimentClassifier)");
            System.out.println("  2 - IA / ML (TribuoSentimentClassifier)");
            System.out.print("Opção: ");

            String option = scanner.nextLine();
            SentimentClassifier classifier;

            if ("2".equals(option.trim())) {
                System.out.println("\n>> Modo selecionado: IA / ML (Tribuo)\n");
                classifier = new TribuoSentimentClassifier();
            } else {
                System.out.println("\n>> Modo selecionado: Regras simples\n");
                classifier = new RuleSentimentClassifier();
            }

            var service = new SentimentAnalysisService(classifier);

            System.out.println("Digite um texto para analisar o sentimento.");
            System.out.println("Digite 'sair' para encerrar.\n");

            while (true) {
                System.out.print("Texto> ");
                String input = scanner.nextLine();

                if (input == null) {
                    System.out.println("\nEntrada encerrada.");
                    break;
                }

                String trimmed = input.trim();
                if ("sair".equalsIgnoreCase(trimmed)) {
                    System.out.println("Encerrando aplicação. Até mais!");
                    break;
                }

                if (trimmed.isEmpty()) {
                    System.out.println("Por favor, digite um texto não vazio.\n");
                    continue;
                }

                try {
                    SentimentResult result = service.analyze(trimmed);
                    System.out.printf(
                            "→ Sentimento: %s (confiança: %.3f)%n%n",
                            result.getLabel(),
                            result.getConfidence()
                    );
                } catch (IllegalArgumentException ex) {
                    System.out.println("Erro na análise: " + ex.getMessage());
                    System.out.println();
                }
            }
        }
    }
}
