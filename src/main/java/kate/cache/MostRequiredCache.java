package kate.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * В кэш памяти попадают и хранятся наиболее востребованные объекты.
 * Объекты, которые вытеснены из памяти, но их срок хранения не истек, переносятся в файл.
 */

public class MostRequiredCache<K, V> extends Cache<K, V> {

    class Counter {

        Integer required = 0;
        Boolean inMemory = true;

        void requiredNow() {
            this.required++;
        }
    }

    private ConcurrentHashMap<K, Counter> requiredCounter = new ConcurrentHashMap<>();

    private void requiredNow(K key) {
        requiredCounter.get(key).requiredNow();

    }

    /**
     * Берем ключ наименее популярного объекта
     */
    protected K getMinKey(boolean inMemory) {
        int minValue = Integer.MAX_VALUE;
        K minKey = null;
        for (Map.Entry<K, Counter> e : requiredCounter.entrySet()) {
            if (e.getValue().inMemory == inMemory && (e.getValue().required < minValue ||
                    (e.getValue().required == minValue &&
                            memoryCache.get(e.getKey()).getAccessed() < memoryCache.get(minKey).getAccessed()))) {
                minKey = e.getKey();
            }
        }
        return minKey;
    }

    @Override
    public void put(K key, V value) {
        super.put(key, value);
        if (requiredCounter.get(key) == null) {
            requiredCounter.put(key, new Counter());
        }
    }

    @Override
    public Optional<V> get(K key) {
        Optional<V> value = super.get(key);
        if (value.isPresent()) requiredNow(key);
        return value;
    }

    @Override
    protected void removeFromMemory(K key) {
        requiredCounter.get(key).inMemory = false;
        memoryCache.remove(key);
    }
}
