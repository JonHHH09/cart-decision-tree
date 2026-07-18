package cart.decisiontree.domain.service;

import cart.decisiontree.domain.model.DecisionNode;
import cart.decisiontree.domain.model.Leaf;
import cart.decisiontree.domain.model.Question;
import cart.decisiontree.domain.model.TreeNode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CartClassifier {

    private CartClassifier() {
    }

    public static TreeNode buildTree(List<? extends List<?>> rows) {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Cannot build a decision tree from an empty training dataset.");
        }
        var split = bestSplit(rows);
        if (split.gain() == 0 || split.question() == null) {
            return new Leaf(rows);
        }
        var partition = partition(rows, split.question());
        return new DecisionNode(split.question(), buildTree(partition.matching()), buildTree(partition.nonMatching()));
    }

    public static Map<Object, Integer> classify(List<?> row, TreeNode node) {
        return switch (node) {
            case Leaf(var predictions) -> predictions;
            case DecisionNode(var question, var trueBranch, var falseBranch) ->
                    classify(row, question.matches(row) ? trueBranch : falseBranch);
        };
    }

    public static Map<Object, String> predictLeaf(Map<?, Integer> counts) {
        var total = counts.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) {
            throw new IllegalArgumentException("Cannot predict from an empty leaf.");
        }
        var probabilities = new LinkedHashMap<Object, String>();
        counts.forEach((label, count) -> probabilities.put(label, "%d%%".formatted(count * 100 / total)));
        return Collections.unmodifiableMap(probabilities);
    }

    static Split bestSplit(List<? extends List<?>> rows) {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Cannot split an empty training dataset.");
        }
        var bestGain = 0.0;
        Question bestQuestion = null;
        var uncertainty = giniImpurity(rows);
        var featureCount = rows.getFirst().size() - 1;
        for (var column = 0; column < featureCount; column++) {
            var values = new LinkedHashMap<Object, Boolean>();
            for (var row : rows) {
                values.putIfAbsent(row.get(column), true);
            }
            for (var value : values.keySet()) {
                var question = new Question(column, value);
                var partition = partition(rows, question);
                if (partition.matching().isEmpty() || partition.nonMatching().isEmpty()) {
                    continue;
                }
                var gain = informationGain(partition.matching(), partition.nonMatching(), uncertainty);
                if (gain > bestGain) {
                    bestGain = gain;
                    bestQuestion = question;
                }
            }
        }
        return new Split(bestGain, bestQuestion);
    }

    static Partition partition(List<? extends List<?>> rows, Question question) {
        var matching = new java.util.ArrayList<List<?>>();
        var nonMatching = new java.util.ArrayList<List<?>>();
        for (var row : rows) {
            (question.matches(row) ? matching : nonMatching).add(row);
        }
        return new Partition(List.copyOf(matching), List.copyOf(nonMatching));
    }

    static double giniImpurity(List<? extends List<?>> rows) {
        if (rows.isEmpty()) {
            return 0.0;
        }
        var counts = new Leaf(rows).predictions();
        var impurity = 1.0;
        for (var count : counts.values()) {
            var probability = (double) count / rows.size();
            impurity -= probability * probability;
        }
        return impurity;
    }

    static double informationGain(List<? extends List<?>> left, List<? extends List<?>> right, double uncertainty) {
        var total = left.size() + right.size();
        if (total == 0) {
            return 0.0;
        }
        var leftFraction = (double) left.size() / total;
        return uncertainty - leftFraction * giniImpurity(left) - (1 - leftFraction) * giniImpurity(right);
    }

    record Split(double gain, Question question) {
    }

    record Partition(List<List<?>> matching, List<List<?>> nonMatching) {
    }
}
