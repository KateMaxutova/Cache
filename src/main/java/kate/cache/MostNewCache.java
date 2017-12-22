package kate.cache;

import java.io.File;
import java.util.Map;

/**
 * В кэш памяти попадают и хранятся наиболее новые объекты.
 * Объекты, которые вытеснены из памяти, но их срок хранения не истек, переносятся в файл.
 */

public class MostNewCache<K, V> extends Cache<K, V> {

    protected K getMinKey(boolean inMemory) {
        long minAccessed = Long.MAX_VALUE;
        K minKey = null;
        if (inMemory) {
            /** Ищем самый старый объект в памяти*/
            for (Map.Entry<K, Bucket<V>> e : memoryCache.entrySet()) {
                if (e.getValue().getAccessed() < minAccessed) {
                    minAccessed = e.getValue().getAccessed();
                    minKey = e.getKey();
                }
            }
        } else {
            /** Ищем самый старый объект в файлах*/
            for (String f : new File(logPath).list()) {
                File file = new File(logPath + File.separator + f);
                if (file.lastModified() < minAccessed) {
                    minAccessed = file.lastModified();
                    minKey = (K) file.getName().replace(".txt", "");
                }
            }
        }
        return minKey;
    }

    @Override
    protected void removeFromMemory(K key) {
        memoryCache.remove(key);
    }

}
