package com.jclavoie.redisproxy.core.cache;

import static org.junit.Assert.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.*;

import java.time.Duration;
import java.util.UUID;

import org.junit.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringJUnitConfig
public class LocalCacheTest
{
  @Test
  public void put_thenGet_returnValue()
  {
    final var cache = new LocalCache<UUID, UUID>(1, 1);
    final var key = UUID.randomUUID();
    final var value = UUID.randomUUID();
    cache.put(key, value);
    assertEquals(value, cache.get(key).get());
  }

  @Test
  public void put_exceededLimit_thenGet_oldestEvicted()
  {
    final var cache = new LocalCache<Integer, Integer>(2, 10);
    cache.put(1, 1);
    cache.put(2, 2);
    cache.put(3, 3);

    assertTrue(cache.get(1).isEmpty());
    assertTrue(cache.get(2).isPresent());
    assertTrue(cache.get(3).isPresent());
  }

  @Test
  @SneakyThrows
  public void get_afterExpiry_returnEmpty()
  {
    final var cache = new LocalCache<Integer, Integer>(5, 2);
    cache.put(5, 5);
    /* Timing is never perfect in test / threads so letting some gap */
    await().atLeast(Duration.ofMillis(1000)).atMost(Duration.ofMillis(3000)).pollDelay(
        Duration.ofMillis(500)).until(
        () -> cache.get(5).isEmpty());
  }
}
