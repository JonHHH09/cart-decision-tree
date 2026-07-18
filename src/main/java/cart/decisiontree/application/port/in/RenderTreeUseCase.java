package cart.decisiontree.application.port.in;

import java.util.List;
import java.util.Map;

public interface RenderTreeUseCase {

    RenderedTree renderTree();

    record RenderedTree(String datasetFingerprint, List<RenderedNode> nodes, String text) {

        public RenderedTree {
            nodes = List.copyOf(nodes);
        }
    }

    record RenderedNode(int depth, String branch, String question, Map<String, Integer> predictions) {

        public RenderedNode {
            predictions = Map.copyOf(predictions);
        }
    }
}
