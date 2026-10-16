package com.ruoyi.framework.config;

import java.util.Collections;
import com.ruoyi.common.core.redis.LocalCache;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.framework.aspectj.RateLimiterAspect;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.mapper.SysConfigMapper;
import com.ruoyi.system.mapper.SysDictDataMapper;
import com.ruoyi.system.mapper.SysDictTypeMapper;
import com.ruoyi.system.service.impl.SysConfigServiceImpl;
import com.ruoyi.system.service.impl.SysDictTypeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheModeTest
{
    @Test
    void defaultsToLocalWithoutRedisBeans()
    {
        try (AnnotationConfigApplicationContext context = context(null))
        {
            context.refresh();
            assertInstanceOf(LocalCache.class, context.getBean(RedisCache.class));
            assertEquals(1, context.getBeansOfType(RedisCache.class).size());
            assertTrue(context.getBeansOfType(RedisTemplate.class).isEmpty());
            assertTrue(context.getBeansOfType(RedisScript.class).isEmpty());
            context.getBean(RedisCache.class).setCacheObject("startup:config", "value");
            assertEquals("value", context.getBean(RedisCache.class).getCacheObject("startup:config"));
        }
    }

    @Test
    void explicitLocalAlsoNeedsNoRedis()
    {
        try (AnnotationConfigApplicationContext context = context("local"))
        {
            context.refresh();
            assertInstanceOf(LocalCache.class, context.getBean(RedisCache.class));
        }
    }

    @Test
    void redisModeRegistersOriginalImplementationAndScriptWithoutConnecting()
    {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        try (AnnotationConfigApplicationContext context = context("redis"))
        {
            context.registerBean(RedisConnectionFactory.class, () -> connectionFactory);
            context.registerBean(CacheManager.class, () -> new ConcurrentMapCacheManager());
            context.refresh();
            assertEquals(RedisCache.class, context.getBean(RedisCache.class).getClass());
            assertEquals(1, context.getBeansOfType(RedisCache.class).size());
            assertNotNull(context.getBean(RedisTemplate.class));
            assertNotNull(context.getBean(RedisScript.class));
            verify(connectionFactory, never()).getConnection();
        }
    }

    @Test
    void unsupportedModeFailsInsteadOfSilentlyChoosingABackend()
    {
        try (AnnotationConfigApplicationContext context = context("invalid"))
        {
            assertThrows(UnsatisfiedDependencyException.class, context::refresh);
        }
    }

    @Test
    void initializesConfigAndDictionaryWithRedisAutoConfigurationButNoRedisServer()
    {
        SysConfig config = new SysConfig();
        config.setConfigKey("startup.test");
        config.setConfigValue("loaded");
        SysConfigMapper configMapper = mock(SysConfigMapper.class);
        when(configMapper.selectConfigList(any())).thenReturn(Collections.singletonList(config));
        SysDictData dict = new SysDictData();
        dict.setDictType("startup_status");
        dict.setDictValue("0");
        dict.setDictLabel("正常");
        dict.setDictSort(1L);
        SysDictDataMapper dictMapper = mock(SysDictDataMapper.class);
        when(dictMapper.selectDictDataList(any())).thenReturn(Collections.singletonList(dict));

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(DataRedisAutoConfiguration.class))
                .withUserConfiguration(RedisCache.class, LocalCache.class, RedisConfig.class,
                        RateLimiterAspect.class, SpringUtils.class, SysConfigServiceImpl.class,
                        SysDictTypeServiceImpl.class)
                .withBean(SysConfigMapper.class, () -> configMapper)
                .withBean(SysDictDataMapper.class, () -> dictMapper)
                .withBean(SysDictTypeMapper.class, () -> mock(SysDictTypeMapper.class))
                .withPropertyValues("spring.data.redis.host=127.0.0.1", "spring.data.redis.port=1")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertInstanceOf(LocalCache.class, context.getBean(RedisCache.class));
                    assertEquals("loaded", context.getBean(SysConfigServiceImpl.class).selectConfigByKey("startup.test"));
                    assertEquals("正常", DictUtils.getDictLabel("startup_status", "0"));
                    verify(configMapper, never()).selectConfig(any());
                    verify(dictMapper, never()).selectDictDataByType(any());
                });
    }

    private AnnotationConfigApplicationContext context(String mode)
    {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        if (mode != null)
        {
            context.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("test", Collections.singletonMap("ruoyi.cache.type", mode)));
        }
        context.register(RedisCache.class, LocalCache.class, RedisConfig.class, RateLimiterAspect.class);
        return context;
    }
}
