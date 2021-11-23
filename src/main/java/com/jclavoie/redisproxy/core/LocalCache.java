package com.jclavoie.redisproxy.core;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.Cache;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class LocalCache
{
  @Autowired
  private Cache<String, String> cache;

  public Mono<String> get(final String key)
  {
    final var value = cache.getIfPresent(key);
    if (value != null)
    {
      log.info("Fetched value for key {} in Local Cache", key);
    }
    else
    {
      log.info("Local Cache missed for key {}", key);
    }
    return Mono.justOrEmpty(value);
  }

  public void put(final String key, final String value)
  {
    cache.put(key, value);
  }
}
