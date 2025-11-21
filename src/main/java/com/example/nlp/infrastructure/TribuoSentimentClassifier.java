package com.example.nlp.infrastructure;

import com.example.nlp.domain.SentimentClassifier;
import com.example.nlp.domain.SentimentLabel;
import com.example.nlp.domain.SentimentResult;
import org.tribuo.Example;
import org.tribuo.Model;
import org.tribuo.classification.Label;
import org.tribuo.data.text.TextFeatureExtractor;
import org.tribuo.data.text.impl.BasicPipeline;
import org.tribuo.data.text.impl.TextFeatureExtractorImpl;
import org.tribuo.util.tokens.universal.UniversalTokenizer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Classificador de sentimentos baseado em modelo Tribuo.
 */
public class TribuoSentimentClassifier implements SentimentClassifier {

    private final Model<Label> model;
    private final TextFeatureExtractor<Label> extractor;

    // Classificador de fallback baseado em regras simples
    private final SentimentClassifier fallbackClassifier = new RuleSentimentClassifier();

    public TribuoSentimentClassifier() {
        this.model = loadOrTrainModel();
        this.extractor = buildExtractor();
    }

    private Model<Label> loadOrTrainModel() {
        Path modelPath = Path.of("data", "model", "tribuo-model.ser");

        try {
            if (Files.exists(modelPath)) {
                System.out.println("Carregando modelo salvo...");
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(modelPath))) {
                    @SuppressWarnings("unchecked")
                    Model<Label> loaded = (Model<Label>) ois.readObject();
                    return loaded;
                }
            } else {
                System.out.println("Nenhum modelo salvo. Treinando um novo...");
                var trainer = new TribuoSentimentTrainer();
                var model = trainer.trainDefault();

                Files.createDirectories(modelPath.getParent());
                try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(modelPath))) {
                    oos.writeObject(model);
                }

                System.out.println("💾 Modelo salvo em " + modelPath.toAbsolutePath());
                return model;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Erro ao carregar ou treinar modelo Tribuo", e);
        }
    }

    private TextFeatureExtractor<Label> buildExtractor() {
        var tokenizer = new UniversalTokenizer();
        var bowPipeline = new BasicPipeline(tokenizer, 2);
        return new TextFeatureExtractorImpl<>(bowPipeline);
    }

    @Override
    public SentimentResult classify(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Texto para classificação não pode ser nulo ou vazio.");
        }

        try {
            // 1) Transformar o texto em Example<Label>
            Example<Label> example = extractor.extract(null, text);

            // 2) Fazer a predição com o modelo
            var prediction = model.predict(example);

            // 3) Classe prevista
            Label output = prediction.getOutput();

            // 4) Pegar scores das saídas
            var scores = prediction.getOutputScores();
            Label scoredLabel = scores.get(output.getLabel());

            double confidence = (scoredLabel != null) ? scoredLabel.getScore() : 1.0;

            // 5) Traduzir Label da Tribuo para enum
            SentimentLabel label;
            switch (output.getLabel()) {
                case "positivo" -> label = SentimentLabel.POSITIVO;
                case "negativo" -> label = SentimentLabel.NEGATIVO;
                default -> label = SentimentLabel.NEUTRO;
            }

            return new SentimentResult(label, confidence);

        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("No features found")) {
                // Fallback: classificando pelas regras (ruleSentiment)
                return fallbackClassifier.classify(text);
            }
            // Se for outro tipo de erro, retorna ele
            throw e;
        }
    }

}
