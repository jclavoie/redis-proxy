package com.jclavoie.redisproxy.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.jclavoie.redisproxy.core.RedisWrapper;
import com.jclavoie.redisproxy.core.cache.LocalCache;

@org.springframework.context.annotation.Configuration
public class Config
{
  @Bean
  public LocalCache<String, String> getLocalCache(
      @Value("${application.redis-proxy.cache.size}") final int cacheSize,
      @Value("${application.redis-proxy.cache.ttl_second}") final int ttlSecond)
  {
    return new LocalCache<>(cacheSize, ttlSecond);
  }

  @Bean
  public RedisWrapper getRedisWrapper()
  {
    return new RedisWrapper();
  }
}
