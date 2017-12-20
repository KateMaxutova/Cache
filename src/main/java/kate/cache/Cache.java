package kate.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

public class Cache<K,V> {

    private ConcurrentHashMap<K, Bucket<V>> memoryCache = new ConcurrentHashMap<>();
    private String logPath = "log";
    private int size;
    private int lifetime = 600;
    private Future<?> evictionFuture = null;

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public synchronized void addCache(Object o) {
        if (memoryCache.size() < size) {
            memoryCache.put(new Bucket(o), LocalDateTime.now());
        } else {
            cleanCache();
            memoryCache = new ConcurrentHashMap<>();
        }
    }

    public void put(K key, V value) {
        // завернуть в корзину, проверить по политике не переполнен ли кэш, заустить evict() если переполнен и полжить корзину по ключу

        throw new UnsupportedOperationException("not implemented");
    }

    // если значение по ключу есть, то вернем завернутым в optional , если нет, то null
    public Optional<V> get(K key) {
        // извлечь корзину, если ее нет вернуть null, если есть проверить по политике не истек ли
        // срок хранения, обновить время последнего доступа accessedNow и вернуть значение
        throw new UnsupportedOperationException("not implemented");
    }

    public void evict() {
        if(evictionFuture != null && !evictionFuture.isDone()) {
            // уже очищаем, значит все придется ждать окончания и еще раз запустить
            try {
                evictionFuture.get();
            } catch (InterruptedException e) {
                // что-то не так пошло.  бросаем вверх
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                // что-то не так пошло.  разворачиваем и бросаем вверх
                throw new RuntimeException(e.getCause());
            }
        }
        evictionFuture = Executors.newSingleThreadExecutor().submit(() -> {
            long minAccessesd = Long.MIN_VALUE;
            K minKey = null;
            for (Map.Entry<K, Bucket<V>> e : memoryCache.entrySet()) {
                if (e.getValue().getAccessed() > minAccessesd) {
                    minKey = e.getKey();
                }
            }
            if (minKey != null) {
                memoryCache.remove(minKey);
                // здесь можно вызвать событие, чтоб оповестить что ключ попар под эвикт. например второй уровень может стереть чтот из файла
            }
        });

    }

    public synchronized void cleanCache() {

        String fileName = "Cache_" + LocalDateTime.now().toString().replace(':', '-').replace('.', '-') + ".txt";

        File directory = new File(logPath);
        directory.mkdir();

        File file = new File(logPath + File.separator + fileName);
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.getAbsoluteFile()));

            for (Map.Entry<Bucket, LocalDateTime> e : memoryCache.entrySet())
                out.writeObject(e.getKey());

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        if (lifetime > 0) {
            cleanCacheFolder();
        }
    }

    public synchronized void cleanCacheFolder() {
        String[] filesList = new File(logPath).list();
        File file;
        for (String f : filesList) {
            file = new File(logPath + File.separator + f);

            if (LocalDateTime.now().minusSeconds(lifetime).isAfter(Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime())) {
                file.delete();
            }

        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Bucket, LocalDateTime> e : memoryCache.entrySet())
            sb.append("Object: ").append(e.getKey().getEntity()).append(" ; timestamp: ").append(e.getValue()).append("\n");
        return sb.toString();
    }
}
