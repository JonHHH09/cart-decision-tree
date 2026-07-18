package cart.decisiontree.domain.service;

import cart.decisiontree.domain.model.DecisionNode;
import cart.decisiontree.domain.model.Leaf;
import cart.decisiontree.domain.model.TreeNode;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class TreeRenderer {

    private TreeRenderer() {
    }

    public static void render(TreeNode node, List<String> featureNames, Consumer<String> output) {
        render(node, featureNames, "", output);
    }

    private static void render(TreeNode node, List<String> featureNames, String spacing, Consumer<String> output) {
        switch (node) {
            case Leaf(var predictions) -> output.accept("%sPredict %s".formatted(spacing, formatCounts(predictions)));
            case DecisionNode(var question, var trueBranch, var falseBranch) -> {
                output.accept(spacing + question.describe(featureNames));
                output.accept(spacing + "--> True:");
                render(trueBranch, featureNames, spacing + "  ", output);
                output.accept(spacing + "--> False:");
                render(falseBranch, featureNames, spacing + "  ", output);
            }
        }
    }

    private static String formatCounts(Map<Object, Integer> counts) {
        return counts.entrySet().stream()
                .map(entry -> "%s: %d".formatted(formatValue(entry.getKey()), entry.getValue()))
                .collect(java.util.stream.Collectors.joining(", ", "{", "}"));
    }

    private static String formatValue(Object value) {
        return value instanceof String text ? "'%s'".formatted(text) : String.valueOf(value);
    }
}
