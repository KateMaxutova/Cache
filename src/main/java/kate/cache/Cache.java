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

public class Cache<K, V> {

    private ConcurrentHashMap<K, Bucket<V>> memoryCache = new ConcurrentHashMap<>();
    private String logPath = "log";
    private int size = 100;
    private int lifetime = 600000;
    private Future<?> evictionFuture = null;

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public int getSize() {
        return memoryCache.size();
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void put(K key, V value) {
        // завернуть в корзину, проверить по политике не переполнен ли кэш, заустить evict() если переполнен и полжить корзину по ключу
        if (memoryCache.size() >= size)
            evict();
        memoryCache.put(key, new Bucket<>(value));

        //throw new UnsupportedOperationException("not implemented");
    }

    // если значение по ключу есть, то вернем завернутым в optional , если нет, то null
    public Optional<V> get(K key) {
        if (memoryCache.get(key) == null) return null;
        if (memoryCache.get(key).getAccessed() > System.currentTimeMillis() - lifetime) {
            memoryCache.get(key).accessedNow();
            return memoryCache.get(key).getEntity();
        } else {
            return null;
        }


        // извлечь корзину, если ее нет вернуть null, если есть проверить по политике не истек ли
        // срок хранения, обновить время последнего доступа accessedNow и вернуть значение
        // throw new UnsupportedOperationException("not implemented");
    }

    public void evict() {
        if (evictionFuture != null && !evictionFuture.isDone()) {
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
            long minAccessed = Long.MIN_VALUE;
            K minKey = null;
            for (Map.Entry<K, Bucket<V>> e : memoryCache.entrySet()) {
                if (e.getValue().getAccessed() > minAccessed) {
                    minKey = e.getKey();
                }
            }
            if (minKey != null) {
                memoryCache.remove(minKey);
                // здесь можно вызвать событие, чтоб оповестить что ключ попар под эвикт. например второй уровень может стереть чтот из файла
            }
        });

    }

    public synchronized void moveToFile() {

        String fileName = "Cache_" + LocalDateTime.now().toString().replace(':', '-').replace('.', '-') + ".txt";

        File directory = new File(logPath);
        directory.mkdir();

        File file = new File(logPath + File.separator + fileName);
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.getAbsoluteFile()));

            for (Map.Entry<K, Bucket<V>> e : memoryCache.entrySet())
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
        for (Map.Entry<K, Bucket<V>> e : memoryCache.entrySet())
            sb.append("Key : ").append(e.getKey()).append(" ; Object : ").append(e.getValue()).append("\n");
        return sb.toString();
    }
}
