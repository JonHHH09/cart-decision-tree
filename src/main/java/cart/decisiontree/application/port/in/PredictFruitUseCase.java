package cart.decisiontree.application.port.in;

import java.util.Map;

public interface PredictFruitUseCase {

    Prediction predict(PredictFruitCommand command);

    record PredictFruitCommand(String color, String shape, int weightGrams, String skin) {
    }

    record Prediction(String predictedFruit, Map<String, Integer> counts, Map<String, String> probabilities) {

        public Prediction {
            counts = Map.copyOf(counts);
            probabilities = Map.copyOf(probabilities);
        }
    }
}
