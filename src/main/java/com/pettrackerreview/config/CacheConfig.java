package com.pettrackerreview.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.Cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        // 创建具有过期时间的缓存
        Collection<Cache> caches = Arrays.asList(
            new ExpiringConcurrentMapCache("blogPosts", 3600000), // 1小时过期
            new ExpiringConcurrentMapCache("reviews", 3600000),   // 1小时过期
            new ExpiringConcurrentMapCache("tags", 3600000)       // 1小时过期
        );
        
        cacheManager.setCaches(caches);
        return cacheManager;
    }
    
    /**
     * 自定义支持过期时间的缓存实现
     */
    public static class ExpiringConcurrentMapCache extends ConcurrentMapCache {
        private final long expirationTime;
        private final ConcurrentMap<Object, Long> expirationMap = new ConcurrentHashMap<>();
        
        public ExpiringConcurrentMapCache(String name, long expirationTime) {
            super(name);
            this.expirationTime = expirationTime;
        }
        
        @Override
        public void put(Object key, Object value) {
            super.put(key, value);
            expirationMap.put(key, System.currentTimeMillis() + expirationTime);
        }
        
        @Override
        public ValueWrapper get(Object key) {
            // 检查是否过期
            Long expiration = expirationMap.get(key);
            if (expiration != null && System.currentTimeMillis() > expiration) {
                // 过期则删除
                super.evict(key);
                expirationMap.remove(key);
                return null;
            }
            return super.get(key);
        }
        
        @Override
        public void evict(Object key) {
            super.evict(key);
            expirationMap.remove(key);
        }
        
        @Override
        public void clear() {
            super.clear();
            expirationMap.clear();
        }
    }
}