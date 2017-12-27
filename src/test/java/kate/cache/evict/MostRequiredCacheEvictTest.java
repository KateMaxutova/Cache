package kate.cache.evict;

import kate.cache.Cache;
import kate.cache.MostRequiredCache;

public class MostRequiredCacheEvictTest extends EvictionTest {

    {
        cache = new Cache(new MostRequiredCache<>());
        folder = "MostRequiredEvictTest";
    }

}