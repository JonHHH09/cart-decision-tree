package cart.decisiontree.domain.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record Leaf(Map<Object, Integer> predictions) implements TreeNode {

    public Leaf {
        predictions = Collections.unmodifiableMap(new LinkedHashMap<>(predictions));
    }

    public Leaf(List<? extends List<?>> rows) {
        this(countLabels(rows));
    }

    private static Map<Object, Integer> countLabels(List<? extends List<?>> rows) {
        var counts = new LinkedHashMap<Object, Integer>();
        for (var row : rows) {
            counts.merge(row.getLast(), 1, Integer::sum);
        }
        return counts;
    }
}
