package com.jclavoie.redisproxy.core.cache;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.Assert;
import org.junit.Test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalCacheTest
{
  @Test
  public void put_exceededLimit_oldestEvicted()
  {
    final var cache = new LocalCache<Integer, Integer>(5, 10);
    cache.put(1, 1);
    cache.put(2, 2);
    cache.put(3, 3);
    cache.put(4, 4);
    cache.put(5, 5);
    cache.put(6, 6);

    assertTrue(cache.get(1).isEmpty());
  }

  @Test
  @SneakyThrows
  public void get_expiryUpdated()
  {
    final int ttl = 1;
    final var cache = new LocalCache<Integer, Integer>(5, ttl);
    cache.put(5, 5);
    final var time1 = Instant.now().plus(Duration.ofSeconds(ttl));
    assertTrue(time1.isAfter(cache.getExpiry(5).get()));
    cache.get(5);
    assertFalse(time1.isAfter(cache.getExpiry(5).get()));
  }

  @Test
  @SneakyThrows
  public void get_afterExpiry_returnEmpty()
  {
    final var cache = new LocalCache<Integer, Integer>(5, 1);
    cache.put(5, 5);
    Thread.sleep(2000);
    Assert.assertTrue(cache.get(5).isEmpty());
  }
}
