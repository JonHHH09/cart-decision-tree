package cart.decisiontree.application.port.in;

import java.util.List;
import java.util.Map;

public interface DatasetSummaryUseCase {

    DatasetSummary summarizeDataset();

    record DatasetSummary(int trainingSamples, int testSamples, List<String> labels,
                          Map<String, List<String>> categoricalValues, int minimumWeightGrams, int maximumWeightGrams) {

        public DatasetSummary {
            labels = List.copyOf(labels);
            categoricalValues = Map.copyOf(categoricalValues);
        }
    }
}
