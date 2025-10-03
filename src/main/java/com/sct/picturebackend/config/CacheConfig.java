package com.sct.picturebackend.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {
    public Cache<String, String> getCache() {
        return Caffeine.newBuilder()
                .initialCapacity(1024)
                .maximumSize(10000L)
                .expireAfterWrite(5L, TimeUnit.MINUTES)
                .build();
    }
}
