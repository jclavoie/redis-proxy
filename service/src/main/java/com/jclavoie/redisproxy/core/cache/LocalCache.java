package com.jclavoie.redisproxy.core.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalCache<K, V>
{
  private final Map<K, V> cache;
  private final Map<K, Instant> expiryCache;
  private final int ttl;

  public LocalCache(final int capacity, final int ttlSecond)
  {
    cache = new LRUCache(capacity);
    expiryCache = new ConcurrentHashMap<>(capacity);
    ttl = ttlSecond;
  }

  public Optional<V> get(final K key)
  {
    final var value = cache.get(key);
    if (value != null)
    {
      final var expiry = expiryCache.getOrDefault(key, Instant.now());
      log.info("now : {}, expiry: {}", Instant.now(), expiry);
      if (expiry.isBefore(Instant.now()))
      {
        log.info("Cache for {} is expired", key);
        cache.remove(key);
        expiryCache.remove(key);
        return Optional.empty();
      }
      log.info("Fetched value for key {} in Local Cache", key);
      return Optional.of(value);
    }
    else
    {
      return Optional.empty();
    }

  }

  /* Blocking here is not ideal because we're in a reactive core but heh */
  public synchronized void put(final K key, final V value)
  {
    cache.put(key, value);
    expiryCache.put(key, Instant.now().plus(Duration.ofSeconds(ttl)));
  }

  protected Optional<Instant> getExpiry(final K key)
  {
    return Optional.ofNullable(expiryCache.get(key));
  }
}
