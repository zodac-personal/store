package com.example.store.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * {@link CacheErrorHandler} to handle availability issues with the cache, not logging the cause
 * {@link RuntimeException} on purpose.
 */
public class ResilientCacheErrorHandler implements CacheErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResilientCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(final RuntimeException exception, final Cache cache, final Object key) {
        LOGGER.warn("Cache unavailable, reading '{}' from the database instead", cache.getName());
    }

    @Override
    public void handleCachePutError(
            final RuntimeException exception, final Cache cache, final Object key, final Object value) {
        LOGGER.warn("Cache unavailable, skipping cache write for '{}'", cache.getName());
    }

    @Override
    public void handleCacheEvictError(final RuntimeException exception, final Cache cache, final Object key) {
        LOGGER.warn("Cache unavailable, skipping cache evict for '{}'", cache.getName());
    }

    @Override
    public void handleCacheClearError(final RuntimeException exception, final Cache cache) {
        LOGGER.warn("Cache unavailable, skipping cache clear for '{}'", cache.getName());
    }
}
