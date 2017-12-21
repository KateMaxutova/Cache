package kate.cache;

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

    @Test
    public void evictionTest() throws InterruptedException {

        cache.setSize(3);
        cache.put("foo", "foo-value");
        Thread.sleep(100);
        cache.put("bar", "bar-value");
        Thread.sleep(100);
        cache.put("bazz", "bazz-value");
        Thread.sleep(100);
        cache.put("foo-bar", "foobar-value");

        // самый старый уходит в файл
        Assert.assertTrue(new File(folder + File.separator + "foo.txt").exists());
        // второй остается в памяти
        Assert.assertEquals("bar-value", cache.get("bar").get());

    }
}
