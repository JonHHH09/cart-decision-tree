package cart.decisiontree.application.service;

import cart.decisiontree.application.model.CartApplicationException;
import cart.decisiontree.application.model.TreeSnapshot;
import cart.decisiontree.application.port.in.DatasetSummaryUseCase;
import cart.decisiontree.application.port.in.EvaluateTreeUseCase;
import cart.decisiontree.application.port.in.PredictFruitUseCase;
import cart.decisiontree.application.port.in.RenderTreeUseCase;
import cart.decisiontree.application.port.in.TrainTreeUseCase;
import cart.decisiontree.application.port.out.FruitDatasetPort;
import cart.decisiontree.application.port.out.TreeModelStore;
import cart.decisiontree.domain.model.DecisionNode;
import cart.decisiontree.domain.model.FruitExample;
import cart.decisiontree.domain.model.Leaf;
import cart.decisiontree.domain.model.TreeNode;
import cart.decisiontree.domain.service.CartClassifier;
import cart.decisiontree.domain.service.TreeRenderer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class CartApplicationService implements DatasetSummaryUseCase, TrainTreeUseCase, PredictFruitUseCase,
        RenderTreeUseCase, EvaluateTreeUseCase {

    private static final List<String> FEATURE_NAMES = List.of("color", "shape", "weight (g)", "skin");
    private static final int MAX_TREE_NODES = FruitDatasetPort.MAX_EXAMPLES * 2 - 1;
    private static final int MAX_TREE_DEPTH = FruitDatasetPort.MAX_EXAMPLES;
    private static final int MAX_RENDERED_TEXT_CHARS = 128_000;

    private final FruitDatasetPort datasets;
    private final TreeModelStore models;

    public CartApplicationService(FruitDatasetPort datasets, TreeModelStore models) {
        this.datasets = datasets;
        this.models = models;
    }

    @Override
    public DatasetSummary summarizeDataset() {
        var training = boundedDataset("training", datasets.trainingExamples());
        var test = boundedDataset("test", datasets.testExamples());
        if (training.isEmpty()) {
            throw new CartApplicationException("DATASET_EMPTY", "The training dataset is empty.");
        }
        var labels = training.stream().map(FruitExample::fruit).distinct().sorted().toList();
        var categorical = new LinkedHashMap<String, List<String>>();
        categorical.put("color", distinctSorted(training, FruitExample::color));
        categorical.put("shape", distinctSorted(training, FruitExample::shape));
        categorical.put("skin", distinctSorted(training, FruitExample::skin));
        var minimumWeight = training.stream().mapToInt(FruitExample::weightGrams).min().orElseThrow();
        var maximumWeight = training.stream().mapToInt(FruitExample::weightGrams).max().orElseThrow();
        return new DatasetSummary(training.size(), test.size(), labels, categorical, minimumWeight, maximumWeight);
    }

    @Override
    public TrainingResult trainTree() {
        var examples = boundedDataset("training", datasets.trainingExamples());
        if (examples.isEmpty()) {
            throw new CartApplicationException("DATASET_EMPTY", "The training dataset is empty.");
        }
        var root = CartClassifier.buildTree(examples.stream().map(FruitExample::trainingRow).toList());
        var snapshot = new TreeSnapshot(fingerprint(examples), examples.size(), root);
        models.replace(snapshot);
        return new TrainingResult(snapshot.datasetFingerprint(), snapshot.trainingSamples(), countNodes(root), depth(root));
    }

    @Override
    public Prediction predict(PredictFruitCommand command) {
        if (command == null) {
            throw new CartApplicationException("INVALID_INPUT", "Prediction input is required.");
        }
        final FruitExample example;
        try {
            example = FruitExample.unlabeled(command.color(), command.shape(), command.weightGrams(), command.skin());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new CartApplicationException("INVALID_INPUT", exception.getMessage());
        }
        var counts = stringCounts(CartClassifier.classify(example.features(), current().root()));
        var probabilities = CartClassifier.predictLeaf(new LinkedHashMap<>(counts)).entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(entry -> String.valueOf(entry.getKey()), Map.Entry::getValue));
        var predicted = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new CartApplicationException("PREDICTION_EMPTY", "The active tree returned no prediction."));
        return new Prediction(predicted, counts, probabilities);
    }

    @Override
    public RenderedTree renderTree() {
        var snapshot = current();
        var nodes = new ArrayList<RenderedNode>();
        flatten(snapshot.root(), 0, "root", nodes);
        var lines = new ArrayList<String>();
        TreeRenderer.render(snapshot.root(), FEATURE_NAMES, lines::add);
        var text = String.join(System.lineSeparator(), lines);
        if (text.length() > MAX_RENDERED_TEXT_CHARS) {
            throw new CartApplicationException("TREE_TOO_LARGE", "The active tree exceeds the rendering limit.");
        }
        return new RenderedTree(snapshot.datasetFingerprint(), nodes, text);
    }

    @Override
    public Evaluation evaluateTree() {
        var snapshot = current();
        var examples = boundedDataset("test", datasets.testExamples());
        if (examples.isEmpty()) {
            throw new CartApplicationException("TEST_DATASET_EMPTY", "The test dataset is empty.");
        }
        var confusion = new TreeMap<String, Map<String, Integer>>();
        var correct = 0;
        for (var example : examples) {
            var counts = stringCounts(CartClassifier.classify(example.features(), snapshot.root()));
            var predicted = counts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElseThrow();
            if (predicted.equals(example.fruit())) {
                correct++;
            }
            confusion.computeIfAbsent(example.fruit(), ignored -> new TreeMap<>()).merge(predicted, 1, Integer::sum);
        }
        return new Evaluation(examples.size(), correct, (double) correct / examples.size(), confusion);
    }

    private TreeSnapshot current() {
        return models.current().orElseThrow(
                () -> new CartApplicationException("MODEL_NOT_TRAINED", "Train the decision tree before using this operation."));
    }

    private static List<String> distinctSorted(List<FruitExample> examples,
                                               java.util.function.Function<FruitExample, String> mapper) {
        return examples.stream().map(mapper).distinct().sorted().toList();
    }

    private static Map<String, Integer> stringCounts(Map<Object, Integer> counts) {
        var result = new TreeMap<String, Integer>();
        counts.forEach((label, count) -> result.put(String.valueOf(label), count));
        return result;
    }

    private static void flatten(TreeNode node, int depth, String branch, List<RenderedNode> nodes) {
        if (depth >= MAX_TREE_DEPTH || nodes.size() >= MAX_TREE_NODES) {
            throw new CartApplicationException("TREE_TOO_LARGE", "The active tree exceeds the rendering limit.");
        }
        switch (node) {
            case Leaf(var predictions) -> nodes.add(new RenderedNode(depth, branch, "", stringCounts(predictions)));
            case DecisionNode(var question, var trueBranch, var falseBranch) -> {
                nodes.add(new RenderedNode(depth, branch, question.describe(FEATURE_NAMES), Map.of()));
                flatten(trueBranch, depth + 1, "true", nodes);
                flatten(falseBranch, depth + 1, "false", nodes);
            }
        }
    }

    private static List<FruitExample> boundedDataset(String name, List<FruitExample> examples) {
        if (examples.size() > FruitDatasetPort.MAX_EXAMPLES) {
            throw new CartApplicationException("DATASET_TOO_LARGE",
                    "The " + name + " dataset exceeds " + FruitDatasetPort.MAX_EXAMPLES + " examples.");
        }
        return examples;
    }

    private static int countNodes(TreeNode node) {
        return switch (node) {
            case Leaf ignored -> 1;
            case DecisionNode(var ignored, var trueBranch, var falseBranch) ->
                    1 + countNodes(trueBranch) + countNodes(falseBranch);
        };
    }

    private static int depth(TreeNode node) {
        return switch (node) {
            case Leaf ignored -> 1;
            case DecisionNode(var ignored, var trueBranch, var falseBranch) ->
                    1 + Math.max(depth(trueBranch), depth(falseBranch));
        };
    }

    private static String fingerprint(List<FruitExample> examples) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            for (var example : examples) {
                update(digest, example.color());
                update(digest, example.shape());
                digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(example.weightGrams()).array());
                update(digest, example.skin());
                update(digest, example.fruit());
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    private static void update(MessageDigest digest, String value) {
        var bytes = value.getBytes(StandardCharsets.UTF_8);
        digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
        digest.update(bytes);
    }
}
