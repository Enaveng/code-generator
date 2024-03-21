package com.enaveng.generatorweb.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine本地缓存  结合redis缓存
 */
@Component
public class CaffeineManager {

    @Resource
    private RedisTemplate redisTemplate;

    Cache<String, Object> localCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    /**
     * 先查找本地缓存 如果缓存没有命中则查询redis
     *
     * @param key
     */
    public Object get(String key) {
        Object localValue = localCache.getIfPresent(key);
        if (localValue != null) {
            return localValue;
        }
        Object value = redisTemplate.opsForValue().get(key);
        //再写入到本地缓存当中
        if (value != null) {
            put(key, value);
        }
        return value;
    }

    /**
     * 添加或者更新一个缓存元素
     *
     * @param key
     * @param value
     */
    public void put(String key, Object value) {
        localCache.put(key, value);
        //同时向redis当中添加缓存
        redisTemplate.opsForValue().set(key, value, 100, TimeUnit.SECONDS);
    }

    /**
     * 移除一个缓存元素
     *
     * @param key
     */
    public void delete(String key) {
        localCache.invalidate(key);
        //同时删除redis缓存
        redisTemplate.delete(key);
    }
}
