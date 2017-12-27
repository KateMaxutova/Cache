package kate.cache.common;

import kate.cache.Cache;
import kate.cache.NewestCache;

public class NewestCacheTest extends CacheTest {

    {
        cache = new Cache<>(new NewestCache<>());
        folder = "NewestCacheTest";
    }

}