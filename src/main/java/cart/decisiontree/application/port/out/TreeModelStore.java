package cart.decisiontree.application.port.out;

import cart.decisiontree.application.model.TreeSnapshot;

import java.util.Optional;

public interface TreeModelStore {

    Optional<TreeSnapshot> current();

    void replace(TreeSnapshot snapshot);
}
