package com.jclavoie.redisproxy.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ProxyService
{
  @Autowired
  private RedisWrapper redisWrapper;
  @Autowired
  private LocalCache localCache;

  public Mono<String> get(final String key)
  {
    return localCache.get(key)
        .switchIfEmpty(getFromRedis(key));
  }

  private Mono<String> getFromRedis(final String key)
  {
    return redisWrapper.get(key)
        .doOnNext(value -> log.info("Fetched value of key {} from redis", key))
        .doOnNext(value -> localCache.put(key, value));
  }
}
