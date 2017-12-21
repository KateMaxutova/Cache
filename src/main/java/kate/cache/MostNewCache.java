package kate.cache;

import java.util.Map;

/**
 * В кэш памяти попадают и хранятся наиболее новые объекты.
 * Объекты, которые вытеснены из памяти, но их срок хранения не истек, переносятся в файл.
 */

public class MostNewCache<K, V> extends Cache<K, V> {

    protected K getMinKey(){
        long minAccessed = Long.MIN_VALUE;
        K minKey = null;
        for (Map.Entry<K, Bucket<V>> e : memoryCache.entrySet()) {
            if (e.getValue().getAccessed() > minAccessed) {
                minKey = e.getKey();
            }
        }
        return minKey;
    }

}
