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

        cache.cleanCacheFolder();
        cache.setSize(3);
        cache.put("foo", "foo-value");
        Thread.sleep(100);
        cache.put("bar", "bar-value");
        Thread.sleep(100);
        cache.put("bazz", "bazz-value");
        Thread.sleep(100);
        cache.put("foo-bar", "foobar-value");
        Thread.sleep(100);
        cache.put("car", "car-value");
        Thread.sleep(100);
        cache.put("boo", "boo-value");
        Thread.sleep(100);
        cache.put("loo", "loo-value");
        Thread.sleep(1000);

        Assert.assertEquals(new File(folder).list().length, 3);
        // самый старый уходит
        Assert.assertFalse(new File(folder + File.separator + "foo.txt").exists());
        // второй остается в файле
        Assert.assertTrue(new File(folder + File.separator + "bar.txt").exists());
        // предпоследний в памяти
        Assert.assertEquals("boo-value", cache.get("boo").get());

    }
}
