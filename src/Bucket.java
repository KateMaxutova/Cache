import java.io.Serializable;
import java.time.LocalDateTime;

public class Bucket implements Serializable {

    private Object entity;
    private LocalDateTime timeStamp = LocalDateTime.now();

    Bucket(Object o) {
        entity = o;
    }

    public Object getEntity() {
        return entity;
    }
}
