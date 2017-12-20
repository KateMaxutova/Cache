package kate.cache;


import org.junit.Assert;
import org.junit.Test;

public class CacheTest {

    @Test
    public void putAndGet() {
        Cache<String, String> cache = new Cache<>();

        Assert.assertNull(cache.get("foo"));

        cache.put("foo", "foo-value");

        String fv1 = cache.get("foo").orElse(null);

        Assert.assertEquals("foo-value", fv1);

        String fv2 = cache.get("foo").orElse(null);

        Assert.assertSame(fv1, fv2);

    }

    @Test
    public void putAndWaitExpirationGet() throws InterruptedException {
        Cache<String, String> cache = new Cache<>();

        cache.setLifetime(500);
        cache.put("foo", "foo-value");

        String fv1 = cache.get("foo").orElse(null);

        Assert.assertEquals("foo-value", fv1);

        Thread.sleep(1000);
        Assert.assertNull(cache.get("foo"));

    }

    @Test
    public void evictionTest() throws InterruptedException {
        Cache<String, String> cache = new Cache<>();

        cache.setSize(3);
        cache.put("foo", "foo-value");
        Thread.sleep(100);
        cache.put("bar", "bar-value");
        Thread.sleep(100);
        cache.put("bazz", "bazz-value");
        Thread.sleep(100);
        cache.put("foo-bar", "foobar-value");

        Assert.assertEquals(cache.getSize(), 3);
        // самый старый уходит
        Assert.assertNull(cache.get("foo"));
        // второй все еще тут
        Assert.assertEquals("bar-value", cache.get("bar").get());

    }

}