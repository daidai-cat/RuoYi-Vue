package com.ruoyi.common.utils;

import java.util.Collections;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.core.redis.LocalCache;
import com.ruoyi.common.utils.spring.SpringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

class DictUtilsCacheTest
{
    @AfterEach
    void clearTestBeanFactory()
    {
        ReflectionTestUtils.setField(SpringUtils.class, "beanFactory", null);
    }

    @Test
    void readsAndClearsTypedLocalDictionaryLists()
    {
        registerCache();
        SysDictData data = new SysDictData();
        data.setDictValue("0");
        data.setDictLabel("正常");
        DictUtils.setDictCache("status", Collections.singletonList(data));
        assertEquals("正常", DictUtils.getDictCache("status").get(0).getDictLabel());
        assertEquals("正常", DictUtils.getDictLabel("status", "0"));
        DictUtils.clearDictCache();
        assertNull(DictUtils.getDictCache("status"));
    }

    @Test
    void stillReadsJsonArraysProducedByRedisSerialization()
    {
        LocalCache cache = registerCache();
        JSONObject data = new JSONObject();
        data.put("dictValue", "1");
        data.put("dictLabel", "停用");
        JSONArray array = new JSONArray();
        array.add(data);
        cache.setCacheObject(CacheConstants.SYS_DICT_KEY + "status", array);
        assertEquals("停用", DictUtils.getDictCache("status").get(0).getDictLabel());
    }

    private LocalCache registerCache()
    {
        LocalCache cache = new LocalCache();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("localCache", cache);
        new SpringUtils().postProcessBeanFactory(beanFactory);
        return cache;
    }
}
