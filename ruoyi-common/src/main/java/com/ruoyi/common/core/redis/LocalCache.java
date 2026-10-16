package com.ruoyi.common.core.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import com.ruoyi.common.constant.Constants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "ruoyi.cache.type", havingValue = "local", matchIfMissing = true)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class LocalCache extends RedisCache
{
    private static final Filter AUTO_TYPE_FILTER = JSONReader.autoTypeFilter(Constants.JSON_WHITELIST_STR);

    private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();

    @Override
    public <T> void setCacheObject(String key, T value)
    {
        entries.put(key, new Entry(snapshot(value), 0));
    }

    @Override
    public <T> void setCacheObject(String key, T value, Integer timeout, TimeUnit unit)
    {
        if (timeout <= 0)
        {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        entries.put(key, new Entry(snapshot(value), System.nanoTime() + unit.toNanos(timeout)));
    }

    @Override
    public boolean expire(String key, long timeout)
    {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    @Override
    public boolean expire(String key, long timeout, TimeUnit unit)
    {
        boolean[] found = { false };
        entries.computeIfPresent(key, (name, entry) -> {
            if (entry.expired())
            {
                return null;
            }
            found[0] = true;
            return timeout <= 0 ? null : new Entry(entry.value, System.nanoTime() + unit.toNanos(timeout));
        });
        return found[0];
    }

    @Override
    public long getExpire(String key)
    {
        Entry entry = getEntry(key);
        if (entry == null)
        {
            return -2;
        }
        return entry.expiresAt == 0 ? -1 : Math.max(0, TimeUnit.NANOSECONDS.toSeconds(entry.expiresAt - System.nanoTime()));
    }

    @Override
    public Boolean hasKey(String key)
    {
        return getEntry(key) != null;
    }

    @Override
    public <T> T getCacheObject(String key)
    {
        Entry entry = getEntry(key);
        return entry == null ? null : snapshot((T) entry.value);
    }

    @Override
    public boolean deleteObject(String key)
    {
        Entry removed = entries.remove(key);
        return removed != null && !removed.expired();
    }

    @Override
    public boolean deleteObject(Collection keys)
    {
        boolean removed = false;
        for (Object key : keys)
        {
            removed |= deleteObject((String) key);
        }
        return removed;
    }

    @Override
    public <T> long setCacheList(String key, List<T> values)
    {
        if (values.isEmpty())
        {
            return getCacheList(key).size();
        }
        Entry updated = entries.compute(key, (name, entry) -> {
            Entry current = entry != null && !entry.expired() ? entry : null;
            List<T> list = current == null ? new ArrayList<>() : new ArrayList<>((List<T>) current.value);
            list.addAll(values);
            return new Entry(list, current == null ? 0 : current.expiresAt);
        });
        return ((List<?>) updated.value).size();
    }

    @Override
    public <T> List<T> getCacheList(String key)
    {
        Entry entry = getEntry(key);
        return entry == null ? new ArrayList<>() : new ArrayList<>((List<T>) entry.value);
    }

    @Override
    public <T> BoundSetOperations<String, T> setCacheSet(String key, Set<T> values)
    {
        throw new UnsupportedOperationException("Bound set operations require Redis cache mode");
    }

    @Override
    public <T> Set<T> getCacheSet(String key)
    {
        throw new UnsupportedOperationException("Set operations require Redis cache mode");
    }

    @Override
    public <T> void setCacheMap(String key, Map<String, T> values)
    {
        if (values == null || values.isEmpty())
        {
            return;
        }
        entries.compute(key, (name, entry) -> {
            Entry current = entry != null && !entry.expired() ? entry : null;
            Map<String, T> map = current == null ? new HashMap<>() : new HashMap<>((Map<String, T>) current.value);
            map.putAll(values);
            return new Entry(map, current == null ? 0 : current.expiresAt);
        });
    }

    @Override
    public <T> Map<String, T> getCacheMap(String key)
    {
        Entry entry = getEntry(key);
        return entry == null ? new HashMap<>() : new HashMap<>((Map<String, T>) entry.value);
    }

    @Override
    public <T> void setCacheMapValue(String key, String field, T value)
    {
        setCacheMap(key, Collections.singletonMap(field, value));
    }

    @Override
    public <T> T getCacheMapValue(String key, String field)
    {
        return this.<T>getCacheMap(key).get(field);
    }

    @Override
    public <T> List<T> getMultiCacheMapValue(String key, Collection<Object> fields)
    {
        Map<String, T> map = getCacheMap(key);
        List<T> values = new ArrayList<>();
        for (Object field : fields)
        {
            values.add(map.get(field));
        }
        return values;
    }

    @Override
    public boolean deleteCacheMapValue(String key, String field)
    {
        boolean[] removed = { false };
        entries.computeIfPresent(key, (name, entry) -> {
            if (entry.expired())
            {
                return null;
            }
            Map<String, Object> map = new HashMap<>((Map<String, Object>) entry.value);
            removed[0] = map.containsKey(field);
            map.remove(field);
            return map.isEmpty() ? null : new Entry(map, entry.expiresAt);
        });
        return removed[0];
    }

    @Override
    public Collection<String> keys(String pattern)
    {
        Set<String> keys = new HashSet<>();
        for (String key : entries.keySet())
        {
            if (getEntry(key) != null && PatternMatchUtils.simpleMatch(pattern, key))
            {
                keys.add(key);
            }
        }
        return keys;
    }

    public long increment(String key, int timeSeconds)
    {
        if (timeSeconds <= 0)
        {
            throw new IllegalArgumentException("Rate limit window must be positive");
        }
        Entry updated = entries.compute(key, (name, entry) -> {
            if (entry == null || entry.expired())
            {
                return new Entry(1L, System.nanoTime() + TimeUnit.SECONDS.toNanos(timeSeconds));
            }
            return new Entry(((Number) entry.value).longValue() + 1, entry.expiresAt);
        });
        return ((Number) updated.value).longValue();
    }

    @Scheduled(fixedDelay = 60000)
    public void removeExpiredEntries()
    {
        entries.forEach((key, entry) -> {
            if (entry.expired())
            {
                entries.remove(key, entry);
            }
        });
    }

    private Entry getEntry(String key)
    {
        return entries.computeIfPresent(key, (name, entry) -> entry.expired() ? null : entry);
    }

    private <T> T snapshot(T value)
    {
        // 与 Redis 保持对象隔离，避免校验失败的请求修改会话缓存。
        return (T) JSON.parseObject(JSON.toJSONString(value, JSONWriter.Feature.WriteClassName),
                Object.class, AUTO_TYPE_FILTER);
    }

    private static final class Entry
    {
        private final Object value;

        private final long expiresAt;

        private Entry(Object value, long expiresAt)
        {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        private boolean expired()
        {
            return expiresAt != 0 && System.nanoTime() - expiresAt >= 0;
        }
    }
}
