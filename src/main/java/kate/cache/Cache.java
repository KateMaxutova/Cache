package kate.cache;

import java.util.Optional;

public class Cache<K, V> {

    Cacheable<K, V> entity;

    public Cache(Cacheable<K, V> way) {
        entity = way;
    }

    public void cleanCacheFolder(){
        entity.cleanCacheFolder();
    }

    public Optional<V> get(K key){
        return (Optional<V>) entity.get(key);
    }

    public void put(K key, V value){
        entity.put((K) key, (V) value);
    }

    public void setLifetime(int lifetime){
        entity.setLifetime(lifetime);
    }

    public void setLogPath(String logPath){
        entity.setLogPath(logPath);
    }

    public void setSize(int size){
        entity.setSize(size);
    }

}
