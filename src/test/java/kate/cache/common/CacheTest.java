package kate.cache.common;

import kate.cache.Cache;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

abstract public class CacheTest {

    protected Cache<String, String> cache;
    protected String folder;

    @Before
    public void setUp(){
        cache.setLogPath(folder);
        cache.cleanCacheFolder();
    }

    @Test
    public void putAndGet() {

        Assert.assertFalse(cache.get("foo").isPresent());

        String fv0 = cache.get("foo").orElse(null);

        Assert.assertNotSame("foo-value", fv0);

        cache.put("foo", "foo-value");

        String fv1 = cache.get("foo").orElse(null);

        Assert.assertEquals("foo-value", fv1);

        String fv2 = cache.get("foo").orElse(null);

        Assert.assertSame(fv1, fv2);

    }

    @Test
    public void putAndWaitExpirationGet() throws InterruptedException {

        cache.setLifetime(500);
        cache.put("foo", "foo-value");

        String fv1 = cache.get("foo").orElse(null);

        Assert.assertEquals("foo-value", fv1);

        Thread.sleep(1000);
        Assert.assertFalse(cache.get("foo").isPresent());

    }

}
