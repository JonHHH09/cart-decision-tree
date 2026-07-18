package cart.decisiontree.application.port.out;

import cart.decisiontree.domain.model.FruitExample;

import java.util.List;

public interface FruitDatasetPort {

    int MAX_EXAMPLES = 512;

    List<FruitExample> trainingExamples();

    List<FruitExample> testExamples();
}
