package com.jclavoie.redisproxy.core.cache;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class LocalCache<K, V>
{
  private final Map<K, V> cache;

  public LocalCache(final int capacity)
  {
    cache = new LRUCache(capacity);
  }

  public Mono<V> get(final K key)
  {
    final var value = cache.get(key);
    if (value != null)
    {
      log.info("Fetched value for key {} in Local Cache", key);
      return Mono.just(value);
    }
    else
    {
      return Mono.empty();
    }
  }

  /* TODO : Make it non blocking */
  public synchronized void put(final K key, final V value)
  {
    cache.put(key, value);
  }
}
