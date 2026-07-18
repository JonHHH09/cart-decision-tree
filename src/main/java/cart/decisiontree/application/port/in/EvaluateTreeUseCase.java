package cart.decisiontree.application.port.in;

import java.util.Map;

public interface EvaluateTreeUseCase {

    Evaluation evaluateTree();

    record Evaluation(int samples, int correct, double accuracy, Map<String, Map<String, Integer>> confusionMatrix) {

        public Evaluation {
            confusionMatrix = confusionMatrix.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                    Map.Entry::getKey, entry -> Map.copyOf(entry.getValue())));
        }
    }
}
