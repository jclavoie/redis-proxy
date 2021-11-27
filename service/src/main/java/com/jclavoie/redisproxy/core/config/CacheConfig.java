package com.jclavoie.redisproxy.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.jclavoie.redisproxy.core.RedisWrapper;
import com.jclavoie.redisproxy.core.cache.LocalCache;

import lombok.extern.slf4j.Slf4j;

@org.springframework.context.annotation.Configuration
@Slf4j
public class CacheConfig
{
  @Bean
  public LocalCache<String, String> getLocalCache(
      @Value("${application.redis-proxy.cache.size:20}") final int cacheSize,
      @Value("${application.redis-proxy.cache.ttl_second:10}") final int ttlSecond)
  {
    return new LocalCache<>(cacheSize, ttlSecond);
  }

  @Bean
  public RedisWrapper getRedisWrapper()
  {
    return new RedisWrapper();
  }
  
}
