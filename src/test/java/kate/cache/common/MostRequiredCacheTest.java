package kate.cache.common;

import kate.cache.Cache;
import kate.cache.MostRequiredCache;

public class MostRequiredCacheTest extends CacheTest {

    {
        cache = new Cache<>(new MostRequiredCache<>());
        folder = "MostRequiredCacheTest";
    }

}