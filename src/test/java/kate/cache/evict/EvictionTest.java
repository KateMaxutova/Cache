package kate.cache.evict;

import kate.cache.Cache;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

abstract public class EvictionTest {

    protected Cache<String, String> cache;
    protected String folder;

    @Before
    public void setUp(){

        cache.setLogPath(folder);
        cache.cleanCacheFolder();

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
        Thread.sleep(100);
        cache.put("car", "car-value");
        Thread.sleep(100);
        cache.put("boo", "boo-value");
        Thread.sleep(100);
        cache.put("loo", "loo-value");
        Thread.sleep(100);

        Assert.assertEquals(3, new File(folder).list().length);
        // самый старый уходит
        Assert.assertFalse(new File(folder + File.separator + "foo.txt").exists());
        // второй остается в файле
        Assert.assertTrue(new File(folder + File.separator + "bar.txt").exists());
        // предпоследний в памяти
        Assert.assertEquals("boo-value", cache.get("boo").get());

    }
}
