package kate.cache;

import java.util.Optional;

public interface Cacheable<K,V> {

    void cleanCacheFolder();

    Optional<V> get(K key);

    void put(K key, V value);

    void setLifetime(int lifetime);

    void setLogPath(String logPath);

    void setSize(int size);

}
