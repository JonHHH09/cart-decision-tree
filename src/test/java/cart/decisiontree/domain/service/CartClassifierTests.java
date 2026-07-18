package cart.decisiontree.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cart.decisiontree.domain.model.DecisionNode;
import cart.decisiontree.domain.model.Leaf;
import cart.decisiontree.domain.model.Question;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

class CartClassifierTests {

    @Test
    void buildsAndClassifiesCategoricalRows() {
        var rows = List.<List<?>>of(
                List.of("red", "small", "yes"),
                List.of("red", "large", "yes"),
                List.of("blue", "small", "no"),
                List.of("blue", "large", "no"));

        var tree = CartClassifier.buildTree(rows);

        assertThat(tree).isInstanceOf(DecisionNode.class);
        assertThat(CartClassifier.classify(List.of("red", "medium"), tree)).containsExactlyEntriesOf(java.util.Map.of("yes", 2));
        assertThat(CartClassifier.predictLeaf(CartClassifier.classify(List.of("blue", "medium"), tree)))
                .containsExactlyEntriesOf(java.util.Map.of("no", "100%"));
    }

    @Test
    void buildsAndClassifiesNumericRows() {
        var tree = CartClassifier.buildTree(List.of(List.of(1, "low"), List.of(2, "low"), List.of(3, "high"), List.of(4, "high")));

        assertThat(CartClassifier.classify(List.of(3), tree)).containsExactlyEntriesOf(java.util.Map.of("high", 2));
        assertThat(CartClassifier.classify(List.of(1), tree)).containsExactlyEntriesOf(java.util.Map.of("low", 2));
    }

    @Test
    void constantRowsProduceLeafAndEmptyInputsUseExpectedErrors() {
        var tree = CartClassifier.buildTree(List.of(List.of("same", "yes"), List.of("same", "yes")));

        assertThat(tree).isInstanceOf(Leaf.class);
        assertThat(CartClassifier.predictLeaf(((Leaf) tree).predictions())).containsExactlyEntriesOf(java.util.Map.of("yes", "100%"));
        assertThatThrownBy(() -> CartClassifier.buildTree(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot build a decision tree from an empty training dataset.");
        assertThatThrownBy(() -> CartClassifier.bestSplit(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot split an empty training dataset.");
        assertThatThrownBy(() -> CartClassifier.predictLeaf(java.util.Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot predict from an empty leaf.");
    }

    @Test
    void rendersDomainFeatureNames() {
        var lines = new ArrayList<String>();

        TreeRenderer.render(CartClassifier.buildTree(List.of(List.of("red", "yes"), List.of("blue", "no"))), List.of("colour"), lines::add);

        assertThat(lines.getFirst()).isEqualTo("Is colour == red?");
        assertThat(lines).anyMatch(line -> line.equals("  Predict {'yes': 1}"));
    }

    @Test
    void retainsFirstEncounteredSplitAndTruncatesProbabilityPercentages() {
        var rows = List.<List<?>>of(
                List.of("red", "round", "yes"),
                List.of("red", "square", "yes"),
                List.of("blue", "round", "no"),
                List.of("blue", "square", "no"));

        var split = CartClassifier.bestSplit(rows);

        assertThat(split.question()).isEqualTo(new Question(0, "red"));
        assertThat(CartClassifier.predictLeaf(java.util.Map.of("yes", 2, "no", 1)))
                .containsExactlyEntriesOf(java.util.Map.of("yes", "66%", "no", "33%"));
    }

    @Test
    void leafDefensivelyCopiesPredictionCounts() {
        var mutableCounts = new LinkedHashMap<Object, Integer>();
        mutableCounts.put("apple", 2);

        var leaf = new Leaf(mutableCounts);
        mutableCounts.put("banana", 1);

        assertThat(leaf.predictions()).containsExactlyEntriesOf(java.util.Map.of("apple", 2));
        assertThatThrownBy(() -> leaf.predictions().put("pear", 1))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
