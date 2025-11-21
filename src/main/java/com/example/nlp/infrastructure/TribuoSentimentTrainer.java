package com.example.nlp.infrastructure;

import org.tribuo.Dataset;
import org.tribuo.MutableDataset;
import org.tribuo.Model;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.classification.evaluation.LabelEvaluation;
import org.tribuo.classification.evaluation.LabelEvaluator;
import org.tribuo.classification.sgd.linear.LinearSGDTrainer;
import org.tribuo.classification.sgd.objectives.LogMulticlass;
import org.tribuo.data.text.DirectoryFileSource;
import org.tribuo.data.text.TextFeatureExtractor;
import org.tribuo.data.text.impl.BasicPipeline;
import org.tribuo.data.text.impl.TextFeatureExtractorImpl;
import org.tribuo.math.optimisers.AdaGrad;
import org.tribuo.util.Util;
import org.tribuo.util.tokens.universal.UniversalTokenizer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Responsável por:
 * - Ler os arquivos em data/train (pastas positivo/negativo/neutro)
 * - Extrair features de texto (Bag-of-Words)
 * - Treinar um modelo de classificação de sentimento com Tribuo
 *
 * Esta classe ainda não está integrada com o nosso domínio SentimentClassifier;
 * por enquanto, é um "laboratório" de treino e avaliação.
 */
public class TribuoSentimentTrainer {

    /**
     * Treina um modelo de sentimento usando os dados na pasta data/train.
     */
    public Model<Label> trainDefault() {
        Path trainPath = Paths.get("data", "train");
        return trainFromDirectory(trainPath);
    }

    /**
     * Treina um modelo de sentimento a partir de um diretório raiz
     * que contém subpastas (uma por classe, com vários .txt).
     */
    public Model<Label> trainFromDirectory(Path trainDir) {
        // 1) Fábrica de rótulos (classes) = POSITIVO/NEGATIVO/NEUTRO
        var labelFactory = new LabelFactory();

        // 2) Tokenizer + pipeline d n-gramas, presença/ausência de palavra
        var tokenizer = new UniversalTokenizer();
        var bowPipeline = new BasicPipeline(tokenizer, 2);
        TextFeatureExtractor<Label> extractor = new TextFeatureExtractorImpl<>(bowPipeline);

        // 3) Fonte de dados em formato de diretório
        var dataSource = new DirectoryFileSource<>(
                trainDir,
                labelFactory,
                extractor
        );

        // 4) Dataset mutável para treino
        Dataset<Label> trainDataset = new MutableDataset<>(dataSource);

        System.out.printf(
                "Treino: %d documentos, %d features, %d classes%n",
                trainDataset.size(),
                trainDataset.getFeatureMap().size(),
                trainDataset.getOutputInfo().size()
        );

        // 5) Trainer: regressão logística multiclass com SGD + AdaGrad
        var trainer = new LinearSGDTrainer(
                new LogMulticlass(),
                new AdaGrad(0.1, 0.0001),
                5,      // número de épocas
                42      // seed (reprodutibilidade)
        );

        long start = System.currentTimeMillis();
        Model<Label> model = trainer.train(trainDataset);
        long end = System.currentTimeMillis();

        System.out.println("Treino concluído em " + Util.formatDuration(start, end));

        // 6) Avaliar usando o próprio treino como teste
        var evaluator = new LabelEvaluator();
        LabelEvaluation evaluation = evaluator.evaluate(model, trainDataset);

        System.out.println("Avaliação (treino como teste):");
        System.out.println(evaluation);


        return model;
    }

    /**
     * Método main apenas para testar o treino .
     */
    public static void main(String[] args) {
        var trainer = new TribuoSentimentTrainer();
        var model = trainer.trainDefault();
        System.out.println("Modelo treinado: " + model.getProvenance().getClassName());
    }
}
