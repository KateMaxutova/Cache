package kate.cache;

import java.io.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * В кэш памяти попадают и хранятся наиболее востребованные объекты.
 * Объекты, которые вытеснены из памяти, но их срок хранения не истек, переносятся в файл.
 */

public class MostRequiredCache<K, V> implements Cacheable<K, V> {

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

    protected ConcurrentHashMap<K, Bucket<V>> memoryCache = new ConcurrentHashMap<>();
    protected String logPath = "cache";
    private int size = 100;
    private int lifetime = 600000;
    protected Future<?> evictionFuture = null;
    protected Future<?> cleanFolderFuture = null;


    protected boolean isAlive(Long lastAccessed) {
        return lastAccessed > System.currentTimeMillis() - getLifetime();
    }

    public int getLifetime() {
        return lifetime;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public void setSize(int size) {
        this.size = size;
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

    protected void removeFromMemory(K key) {
        requiredCounter.get(key).inMemory = false;
        memoryCache.remove(key);
    }

    public void put(K key, V value) {
        if (memoryCache.size() >= size) {
            evict();
            try {
                evictionFuture.get();
            } catch (InterruptedException e) {
                // что-то не так пошло.  бросаем вверх
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                // что-то не так пошло.  разворачиваем и бросаем вверх
                throw new RuntimeException(e.getCause());
            }
            memoryCache.put(key, new Bucket<>(value));
        } else {
            memoryCache.put(key, new Bucket<>(value));
        }
        if (requiredCounter.get(key) == null) {
            requiredCounter.put(key, new Counter());
        }
    }

    public Optional<V> get(K key) {
        /** Если объект жив в кэше памяти, то достаем*/
        if (memoryCache.get(key) != null && isAlive(memoryCache.get(key).getAccessed())) {
            memoryCache.get(key).accessedNow();
            requiredNow(key);
            return memoryCache.get(key).getEntity();
        } else if (!memoryCache.isEmpty() && findFile(key).isPresent()) {
            /** Если объект находится в файле*/

            Bucket<V> readObject = null;
            try {
                /** то читаем */
                readObject = (Bucket<V>) new ObjectInputStream(new FileInputStream(findFile(key).get().getAbsolutePath())).readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            requiredNow(key);
            return readObject.getEntity();
        } else {
            /** Если ни в одном кэше его нет, возвращаем null*/
            return Optional.empty();

        }
    }

    /**
     * Очищает кэш памяти
     */
    protected void evict() {
        evictionFuture = Executors.newSingleThreadExecutor().submit(() -> {
            K minKey = getMinKey(true);
            if (minKey != null) {
                /** Предварительная чистка файлов */
                if (cleanFolderFuture != null && !cleanFolderFuture.isDone()) {
                    // уже очищаем, значит ждём окончания и еще раз запускаем
                    try {
                        cleanFolderFuture.get();
                    } catch (InterruptedException e) {
                        // что-то не так пошло.  бросаем вверх
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        // что-то не так пошло.  разворачиваем и бросаем вверх
                        throw new RuntimeException(e.getCause());
                    }
                }
                cleanCacheFolder();
                if (isAlive(memoryCache.get(minKey).getAccessed())) {
                    /** Проверяем существует ли файл с нашим объектом
                     *   Если нет, записываем */
                    if (!findFile(minKey).isPresent()) {

                        String fileName = minKey + ".txt";

                        new File(logPath).mkdir();

                        File file = new File(logPath + File.separator + fileName);
                        try {
                            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.getAbsoluteFile()));
                            out.writeObject(memoryCache.get(minKey));
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.exit(-1);
                        }
                    }
                }
                removeFromMemory(minKey);
            }
        });
    }

    /**
     * Очищение файлового кэша
     */
    public void cleanCacheFolder() {
        cleanFolderFuture = Executors.newSingleThreadExecutor().submit(() -> {
            String[] fileList = new File(logPath).list();
            for (String f : fileList) {
                File file = new File(logPath + File.separator + f);
                /* Проверяем нужно ли хранить файлы*/
                if (!isAlive(file.lastModified())) {
                    file.delete();
                }
            }
            while (fileList.length > size) {
                new File(logPath + File.separator + getMinKey(false).toString() + ".txt").delete();
            }
        });
    }

    protected Optional<File> findFile(K key) {
        File cacheFolder = new File(logPath);
        if (cacheFolder.exists()) {
            for (String f : cacheFolder.list()) {
                if (f.contains(key.toString()))
                    return Optional.of(new File(logPath + File.separator + f));
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<K, Bucket<V>> e : memoryCache.entrySet()) {
            sb.append("Key : ").append(e.getKey()).append(" ; Object : ");
            if (e.getValue().getEntity().isPresent()) {
                sb.append(e.getValue().getEntity().get());
            } else {
                sb.append("null");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
