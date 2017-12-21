package kate.cache;

import java.io.Serializable;
import java.util.Optional;

public class Bucket<V> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final V entity;
    private final long created = System.currentTimeMillis();
    private long accessed = System.currentTimeMillis();

    Bucket(V o) {
        entity = o;
    }

    public long getAccessed() {
        return accessed;
    }

    public Optional<V> getEntity() {
        return Optional.ofNullable(entity);
    }

    public long getCreated() {
        return created;
    }

    public void accessedNow() {
        this.accessed = System.currentTimeMillis();
    }
}
