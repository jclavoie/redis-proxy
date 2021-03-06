package com.jclavoie.redisproxy.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jclavoie.redisproxy.core.cache.LocalCache;
import com.jclavoie.redisproxy.core.cache.RedisCache;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ProxyService
{
  private final RedisCache redisWrapper;
  private final LocalCache<String, String> localCache;

  @Autowired
  public ProxyService(final RedisCache redisWrapper, final LocalCache localCache)
  {
    this.redisWrapper = redisWrapper;
    this.localCache = localCache;
  }

  public Mono<String> get(final String key)
  {
    return Mono.justOrEmpty(localCache.get(key))
        .switchIfEmpty(Mono.defer(() -> getFromRedis(key)));
  }

  private Mono<String> getFromRedis(final String key)
  {
    return redisWrapper.get(key)
        .doOnNext(value -> log.info("Fetched value of key {} from redis", key))
        .doOnNext(value -> localCache.put(key, value));
  }
}
