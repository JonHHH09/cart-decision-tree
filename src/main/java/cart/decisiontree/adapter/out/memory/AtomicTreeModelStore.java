package cart.decisiontree.adapter.out.memory;

import cart.decisiontree.application.model.TreeSnapshot;
import cart.decisiontree.application.port.out.TreeModelStore;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

@Component
public final class AtomicTreeModelStore implements TreeModelStore {

    private final AtomicReference<TreeSnapshot> current = new AtomicReference<>();

    @Override
    public Optional<TreeSnapshot> current() {
        return Optional.ofNullable(current.get());
    }

    @Override
    public void replace(TreeSnapshot snapshot) {
        current.set(java.util.Objects.requireNonNull(snapshot, "snapshot is required"));
    }
}
