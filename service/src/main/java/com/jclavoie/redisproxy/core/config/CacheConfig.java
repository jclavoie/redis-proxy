package com.jclavoie.redisproxy.core.config;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.jclavoie.redisproxy.core.RedisWrapper;
import com.jclavoie.redisproxy.core.cache.LocalCache;

@org.springframework.context.annotation.Configuration
public class CacheConfig
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

  @Bean
  public Bulkhead getRestThrottlingLimiter(
      @Value("${application.redis-proxy.max_concurrent_request}") final int maxNbRequests)
  {
    final var config =
        BulkheadConfig.custom()
            .maxConcurrentCalls(maxNbRequests)
            .maxWaitDuration(Duration.ZERO)
            .build();
    final var registry = BulkheadRegistry.of(config);
    return registry.bulkhead("concurrent_get");
  }

}
