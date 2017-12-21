package kate.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * В кэш памяти попадают и хранятся наиболее востребованные объекты.
 * Объекты, которые вытеснены из памяти, но их срок хранения не истек, переносятся в файл.
 */

public class MostRequiredCache<K, V> extends Cache<K, V>  {

    private ConcurrentHashMap<K, Integer> requiredCounter = new ConcurrentHashMap<>();

    private void requiredNow(K key) {
        int value = requiredCounter.get(key) + 1;
        requiredCounter.put(key, value);
    }

    /** Берем ключ наименее популярного объекта*/
    protected K getMinKey(){
        int minValue = Integer.MAX_VALUE;
        K minKey = null;
        for (Map.Entry<K, Integer> e : requiredCounter.entrySet()) {
            if ((e.getValue() < minValue) || (e.getValue() == minValue &&
                memoryCache.get(e.getKey()).getAccessed()<memoryCache.get(minKey).getAccessed())){
                minKey = e.getKey();
            }
        }
        return minKey;
    }

    @Override
    public void put(K key, V value) {
        super.put(key, value);
        if(requiredCounter.get(key) == null){
            requiredCounter.put(key, 0);
        }
    }

    @Override
    public Optional<V> get(K key) {
        Optional<V> value = super.get(key);
        if (value.isPresent()) requiredNow(key);
        return value;
    }

}
