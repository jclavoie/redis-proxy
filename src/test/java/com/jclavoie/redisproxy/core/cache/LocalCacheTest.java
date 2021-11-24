package com.jclavoie.redisproxy.core.cache;

import org.junit.Test;

import reactor.test.StepVerifier;

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

    StepVerifier.create(cache.get(1))
        .expectNextCount(0)
        .verifyComplete();
  }
}
