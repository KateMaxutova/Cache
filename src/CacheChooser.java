import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheChooser {

    private ConcurrentHashMap<Bucket, LocalDateTime> memoryCache = new ConcurrentHashMap<>();
    private String logPath = "log";
    private int size;
    private int lifetime = 600;

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
