package cart.decisiontree.adapter.out.memory;

import static org.assertj.core.api.Assertions.assertThat;

import cart.decisiontree.application.model.TreeSnapshot;
import cart.decisiontree.domain.model.Leaf;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class AtomicTreeModelStoreTests {

    @Test
    void atomicallyPublishesCompleteImmutableSnapshots() throws Exception {
        var store = new AtomicTreeModelStore();
        var ready = new CountDownLatch(1);
        var published = new CountDownLatch(1);
        var snapshot = new TreeSnapshot("fingerprint", 1, new Leaf(Map.of("apple", 1)));

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                ready.countDown();
                store.replace(snapshot);
                published.countDown();
            });
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(published.await(5, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(store.current()).containsSame(snapshot);
    }

    @Test
    void publishedTreeDoesNotRetainMutableLeafState() {
        var store = new AtomicTreeModelStore();
        var mutableCounts = new LinkedHashMap<Object, Integer>();
        mutableCounts.put("apple", 1);
        var snapshot = new TreeSnapshot("fingerprint", 1, new Leaf(mutableCounts));

        store.replace(snapshot);
        mutableCounts.put("banana", 1);

        var publishedLeaf = (Leaf) store.current().orElseThrow().root();
        assertThat(publishedLeaf.predictions()).containsExactlyEntriesOf(Map.of("apple", 1));
    }
}
