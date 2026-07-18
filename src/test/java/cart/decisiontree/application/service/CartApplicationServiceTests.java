package cart.decisiontree.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cart.decisiontree.application.model.CartApplicationException;
import cart.decisiontree.application.model.TreeSnapshot;
import cart.decisiontree.application.port.in.PredictFruitUseCase.PredictFruitCommand;
import cart.decisiontree.application.port.out.FruitDatasetPort;
import cart.decisiontree.application.port.out.TreeModelStore;
import cart.decisiontree.domain.model.FruitExample;
import cart.decisiontree.domain.model.DecisionNode;
import cart.decisiontree.domain.model.Leaf;
import cart.decisiontree.domain.model.Question;
import cart.decisiontree.domain.model.TreeNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class CartApplicationServiceTests {

    private static final List<FruitExample> TRAINING = List.of(
            new FruitExample("red", "round", 180, "smooth", "apple"),
            new FruitExample("green", "round", 160, "smooth", "apple"),
            new FruitExample("yellow", "long", 120, "smooth", "banana"),
            new FruitExample("yellow", "curved", 130, "smooth", "banana"));

    @Test
    void exposesTheSharedWorkflowAfterTraining() {
        var store = new FakeTreeModelStore();
        var service = new CartApplicationService(new FakeDatasets(TRAINING, TRAINING), store);

        var summary = service.summarizeDataset();
        var training = service.trainTree();
        var prediction = service.predict(new PredictFruitCommand(" RED ", "round", 175, "smooth"));
        var rendering = service.renderTree();
        var evaluation = service.evaluateTree();

        assertThat(summary.trainingSamples()).isEqualTo(4);
        assertThat(summary.labels()).containsExactly("apple", "banana");
        assertThat(training.datasetFingerprint()).hasSize(64);
        assertThat(training.nodeCount()).isGreaterThan(1);
        assertThat(prediction.predictedFruit()).isEqualTo("apple");
        assertThat(rendering.nodes()).isNotEmpty();
        assertThat(rendering.text()).contains("Predict");
        assertThat(evaluation.samples()).isEqualTo(4);
        assertThat(evaluation.correct()).isEqualTo(4);
        assertThat(evaluation.accuracy()).isEqualTo(1.0);
        assertThat(store.current()).isPresent();
    }

    @Test
    void reportsStableSanitizedApplicationErrors() {
        var service = new CartApplicationService(new FakeDatasets(TRAINING, List.of()), new FakeTreeModelStore());

        assertThatThrownBy(service::renderTree)
                .isInstanceOfSatisfying(CartApplicationException.class, error -> {
                    assertThat(error.code()).isEqualTo("MODEL_NOT_TRAINED");
                    assertThat(error).hasMessage("Train the decision tree before using this operation.");
                });

        service.trainTree();

        assertThatThrownBy(() -> service.predict(new PredictFruitCommand("", "round", 100, "smooth")))
                .isInstanceOfSatisfying(CartApplicationException.class,
                        error -> assertThat(error.code()).isEqualTo("INVALID_INPUT"));
        assertThatThrownBy(service::evaluateTree)
                .isInstanceOfSatisfying(CartApplicationException.class,
                        error -> assertThat(error.code()).isEqualTo("TEST_DATASET_EMPTY"));
    }

    @Test
    void rejectsDatasetsBeyondTheBoundBeforeTrainingOrRendering() {
        var oversized = IntStream.rangeClosed(1, FruitDatasetPort.MAX_EXAMPLES + 1)
                .mapToObj(index -> new FruitExample("red", "round", index, "smooth", "apple"))
                .toList();
        var service = new CartApplicationService(new FakeDatasets(oversized, TRAINING), new FakeTreeModelStore());

        assertThatThrownBy(service::summarizeDataset)
                .isInstanceOfSatisfying(CartApplicationException.class, error -> {
                    assertThat(error.code()).isEqualTo("DATASET_TOO_LARGE");
                    assertThat(error).hasMessage("The training dataset exceeds 512 examples.");
                });
        assertThatThrownBy(service::trainTree)
                .isInstanceOfSatisfying(CartApplicationException.class,
                        error -> assertThat(error.code()).isEqualTo("DATASET_TOO_LARGE"));
    }

    @Test
    void rejectsTreesBeyondTheRenderingDepthBound() {
        var store = new FakeTreeModelStore();
        TreeNode root = new Leaf(Map.of("apple", 1));
        for (var depth = 0; depth < FruitDatasetPort.MAX_EXAMPLES; depth++) {
            root = new DecisionNode(new Question(0, "red"), root, new Leaf(Map.of("banana", 1)));
        }
        store.replace(new TreeSnapshot("fingerprint", 1, root));
        var service = new CartApplicationService(new FakeDatasets(TRAINING, TRAINING), store);

        assertThatThrownBy(service::renderTree)
                .isInstanceOfSatisfying(CartApplicationException.class, error -> {
                    assertThat(error.code()).isEqualTo("TREE_TOO_LARGE");
                    assertThat(error).hasMessage("The active tree exceeds the rendering limit.");
                });
    }

    private record FakeDatasets(List<FruitExample> training, List<FruitExample> test) implements FruitDatasetPort {

        @Override
        public List<FruitExample> trainingExamples() {
            return List.copyOf(training);
        }

        @Override
        public List<FruitExample> testExamples() {
            return List.copyOf(test);
        }
    }

    private static final class FakeTreeModelStore implements TreeModelStore {

        private TreeSnapshot snapshot;

        @Override
        public Optional<TreeSnapshot> current() {
            return Optional.ofNullable(snapshot);
        }

        @Override
        public void replace(TreeSnapshot snapshot) {
            this.snapshot = snapshot;
        }
    }
}
