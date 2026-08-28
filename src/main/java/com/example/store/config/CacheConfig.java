package com.example.store.config;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerCustomizer() {
        final RedisSerializationContext.SerializationPair<Object> jsonSerializer =
                RedisSerializationContext.SerializationPair.fromSerializer(GenericJacksonJsonRedisSerializer.builder()
                        .enableUnsafeDefaultTyping()
                        .build());
        return builder -> builder.transactionAware()
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(jsonSerializer));
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new ResilientCacheErrorHandler();
    }
}
