package kate.cache.evict;

import kate.cache.Cache;
import kate.cache.NewestCache;

public class NewestCacheEvictTest extends EvictionTest {

    {
        cache = new Cache<>(new NewestCache<>());
        folder = "NewestEvictTest";
    }

}