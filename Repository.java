import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Reusable generic repository.
 * K = key type, V = stored type. A HashMap gives O(1) lookup by key and an
 * ArrayList keeps insertion order for iteration and reports.
 */
public class Repository<K, V> {

    private final Map<K, V> index = new HashMap<>();
    private final List<V> items = new ArrayList<>();
    private final Function<V, K> keyOf;

    public Repository(Function<V, K> keyOf) {
        this.keyOf = keyOf;
    }

    public void save(V value) throws DuplicateIdException {
        K key = keyOf.apply(value);
        if (index.containsKey(key)) {
            throw new DuplicateIdException(key);
        }
        index.put(key, value);
        items.add(value);
    }

    public Optional<V> findById(K key) {
        return Optional.ofNullable(index.get(key));
    }

    /** Read-only view of the backing list: no copy is made. */
    public List<V> findAll() {
        return Collections.unmodifiableList(items);
    }

    public List<V> findWhere(Predicate<V> filter) {
        List<V> result = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            V v = items.get(i);
            if (filter.test(v)) {
                result.add(v);
            }
        }
        return result;
    }

    public boolean remove(K key) {
        V removed = index.remove(key);
        return removed != null && items.remove(removed);
    }

    public int size() {
        return items.size();
    }
}
