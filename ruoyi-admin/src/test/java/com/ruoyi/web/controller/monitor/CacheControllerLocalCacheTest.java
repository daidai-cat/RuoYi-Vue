package com.ruoyi.web.controller.monitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeSet;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.LocalCache;
import com.ruoyi.system.domain.SysCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

class CacheControllerLocalCacheTest
{
    private final LocalCache cache = new LocalCache();
    private final CacheController controller = new CacheController();

    @BeforeEach
    void setUp()
    {
        ReflectionTestUtils.setField(controller, "redisCache", cache);
    }

    @Test
    void reportsLocalModeAndActualKeyCountWithoutRedis() throws Exception
    {
        cache.setCacheObject("test:first", "value");
        Map<?, ?> info = (Map<?, ?>) controller.getInfo().get(AjaxResult.DATA_TAG);
        assertEquals("local", info.get("type"));
        assertEquals(1, ((Number) info.get("dbSize")).intValue());
        assertEquals(Collections.emptyMap(), info.get("info"));
        assertEquals(Collections.emptyList(), info.get("commandStats"));
    }

    @Test
    void returnsOnlyMatchingKeysInOrder()
    {
        cache.setCacheObject("test:z", "last");
        cache.setCacheObject("test:a", "first");
        cache.setCacheObject("other:a", "other");
        assertEquals(new TreeSet<>(Arrays.asList("test:a", "test:z")),
                controller.getCacheKeys("test:").get(AjaxResult.DATA_TAG));
    }

    @Test
    void serializesLocalObjectsForCacheViewer()
    {
        cache.setCacheObject("test:object", Collections.singletonMap("name", "example"));
        SysCache value = (SysCache) controller.getCacheValue("test:", "test:object").get(AjaxResult.DATA_TAG);
        assertEquals("example", JSON.parseObject(value.getCacheValue()).getString("name"));
        assertEquals("object", value.getCacheKey());
    }

    @Test
    void clearsOnlySelectedCacheName()
    {
        cache.setCacheObject("test:first", "first");
        cache.setCacheObject("test:second", "second");
        cache.setCacheObject("other:first", "other");
        assertTrue(controller.clearCacheName("test:").isSuccess());
        assertTrue(cache.keys("test:*").isEmpty());
        assertTrue(cache.hasKey("other:first"));
    }

    @Test
    void clearsOneKeyThenAllRemainingKeys()
    {
        cache.setCacheObject("test:first", "first");
        cache.setCacheObject("test:second", "second");
        assertTrue(controller.clearCacheKey("test:first").isSuccess());
        assertFalse(cache.hasKey("test:first"));
        assertTrue(cache.hasKey("test:second"));
        assertTrue(controller.clearCacheAll().isSuccess());
        assertTrue(cache.keys("*").isEmpty());
        assertTrue(controller.clearCacheAll().isSuccess());
    }
}
