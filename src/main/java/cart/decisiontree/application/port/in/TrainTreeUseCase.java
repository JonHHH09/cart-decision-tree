package cart.decisiontree.application.port.in;

public interface TrainTreeUseCase {

    TrainingResult trainTree();

    record TrainingResult(String datasetFingerprint, int trainingSamples, int nodeCount, int depth) {
    }
}
