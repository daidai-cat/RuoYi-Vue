package com.ruoyi.common.core.redis;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LocalCacheTest
{
    private final LocalCache cache = new LocalCache();

    @Test
    void unsavedSessionMutationsDoNotChangeTheCachedUser()
    {
        SysUser user = new SysUser();
        user.setUserName("example");
        user.setEmail("saved@example.test");
        LoginUser session = new LoginUser(1L, 1L, user, Collections.singleton("system:user:query"));
        cache.setCacheObject("session", session);

        LoginUser requestSession = cache.getCacheObject("session");
        requestSession.getUser().setEmail("rejected@example.test");
        LoginUser savedSession = cache.getCacheObject("session");
        assertEquals("saved@example.test", savedSession.getUser().getEmail());
        assertEquals(Collections.singleton("system:user:query"), savedSession.getPermissions());
    }

    @Test
    void writingMutableValuesStoresAnIndependentSnapshot()
    {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("time", 1000L);
        value.put("params", new ArrayList<>(Collections.singletonList("saved")));
        cache.setCacheObject("repeat", value);
        ((List<String>) value.get("params")).clear();

        Map<String, Object> cached = cache.getCacheObject("repeat");
        assertEquals(Collections.singletonList("saved"), cached.get("params"));
        assertInstanceOf(Long.class, cached.get("time"));
    }

    @Test
    void expiresValuesAndReportsMissingKeys() throws InterruptedException
    {
        assertEquals(-2, cache.getExpire("missing"));
        cache.setCacheObject("captcha", "1234", 20, TimeUnit.MILLISECONDS);
        assertTrue(cache.hasKey("captcha"));
        Thread.sleep(60);
        assertNull(cache.getCacheObject("captcha"));
        assertFalse(cache.hasKey("captcha"));
        assertEquals(-2, cache.getExpire("captcha"));
    }

    @Test
    void overwritingWithoutTimeoutRemovesOldExpiration() throws InterruptedException
    {
        cache.setCacheObject("config", "old", 20, TimeUnit.MILLISECONDS);
        cache.setCacheObject("config", "new");
        Thread.sleep(60);
        assertEquals("new", cache.getCacheObject("config"));
        assertEquals(-1, cache.getExpire("config"));
    }

    @Test
    void expirationUpdatesOnlyExistingKeys() throws InterruptedException
    {
        assertFalse(cache.expire("missing", 1));
        cache.setCacheObject("session", "user");
        assertTrue(cache.expire("session", 20, TimeUnit.MILLISECONDS));
        Thread.sleep(60);
        assertFalse(cache.hasKey("session"));
        cache.setCacheObject("immediate", "user");
        assertTrue(cache.expire("immediate", 0));
        assertFalse(cache.hasKey("immediate"));
    }

    @Test
    void findsLiteralPrefixesAndDeletesMatchingKeys() throws InterruptedException
    {
        cache.setCacheObject("login_tokens:one", 1);
        cache.setCacheObject("login_tokens:two", 2);
        cache.setCacheObject("login_tokens:expired", 3, 1, TimeUnit.MILLISECONDS);
        cache.setCacheObject("config:one", 4);
        cache.setCacheObject("config.v1:one", 5);
        cache.setCacheObject("configXv1:one", 6);
        Thread.sleep(20);
        assertEquals(2, cache.keys("login_tokens:*").size());
        assertEquals(Collections.singleton("config.v1:one"), cache.keys("config.v1:*"));
        assertTrue(cache.deleteObject(cache.keys("login_tokens:*")));
        assertFalse(cache.deleteObject("login_tokens:one"));
        assertTrue(cache.hasKey("config:one"));
        assertFalse(cache.deleteObject(Collections.emptyList()));
    }

    @Test
    void incrementIsAtomicAcrossConcurrentRequests() throws Exception
    {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try
        {
            List<Future<?>> futures = new ArrayList<>();
            for (int worker = 0; worker < 8; worker++)
            {
                futures.add(executor.submit(() -> {
                    for (int request = 0; request < 100; request++)
                    {
                        cache.increment("rate", 60);
                    }
                }));
            }
            for (Future<?> future : futures)
            {
                future.get();
            }
            assertEquals(801, cache.increment("rate", 60));
        }
        finally
        {
            executor.shutdownNow();
        }
    }

    @Test
    void incrementKeepsOriginalWindowAndResetsAfterExpiry() throws InterruptedException
    {
        assertEquals(1, cache.increment("rate", 2));
        Thread.sleep(1100);
        assertEquals(2, cache.increment("rate", 2));
        assertEquals(0, cache.getExpire("rate"));
        Thread.sleep(1000);
        assertEquals(1, cache.increment("rate", 2));
    }

    @Test
    void listWritesAppendAndReadsReturnIndependentCopies()
    {
        assertTrue(cache.getCacheList("list").isEmpty());
        assertEquals(2, cache.setCacheList("list", Arrays.asList("a", "b")));
        assertEquals(3, cache.setCacheList("list", Collections.singletonList("c")));
        List<String> values = cache.getCacheList("list");
        values.clear();
        assertEquals(Arrays.asList("a", "b", "c"), cache.getCacheList("list"));
    }

    @Test
    void hashOperationsMergeAndPreserveRequestedOrder()
    {
        Map<String, Integer> first = new LinkedHashMap<>();
        first.put("a", 1);
        cache.setCacheMap("hash", first);
        cache.setCacheMapValue("hash", "b", 2);
        cache.setCacheMap("hash", Collections.singletonMap("c", 3));
        assertEquals(3, cache.getCacheMap("hash").size());
        assertEquals(Integer.valueOf(2), cache.<Integer>getCacheMapValue("hash", "b"));
        assertEquals(Arrays.asList(3, null, 1), cache.getMultiCacheMapValue("hash", Arrays.asList("c", "missing", "a")));
        assertTrue(cache.deleteCacheMapValue("hash", "a"));
        assertFalse(cache.deleteCacheMapValue("hash", "missing"));
        assertEquals(2, cache.getCacheMap("hash").size());
        cache.getCacheMap("hash").clear();
        assertEquals(2, cache.getCacheMap("hash").size());
        assertTrue(cache.deleteCacheMapValue("hash", "b"));
        assertTrue(cache.deleteCacheMapValue("hash", "c"));
        assertFalse(cache.hasKey("hash"));
    }

    @Test
    void unsupportedBoundSetOperationsFailWithoutAccessingRedis()
    {
        assertThrows(UnsupportedOperationException.class,
                () -> cache.setCacheSet("set", Collections.singleton("a")));
        assertThrows(UnsupportedOperationException.class, () -> cache.getCacheSet("set"));
    }
}
