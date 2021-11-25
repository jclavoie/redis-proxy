package com.jclavoie.redisproxy.core;

import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.jclavoie.redisproxy.core.cache.LocalCache;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringJUnitConfig
public class ProxyServiceTest
{
  ProxyService proxyService;
  LocalCache<String, String> localCache;
  RedisWrapper redisWrapper;

  @BeforeEach
  public void init()
  {
    localCache = mock(LocalCache.class);
    redisWrapper = mock(RedisWrapper.class);
    proxyService = new ProxyService(redisWrapper, localCache);
  }

  @Test
  public void get_foundInCache_redisNotCalled()
  {
    final var key = UUID.randomUUID().toString();
    when(localCache.get(key)).thenReturn(Optional.of("Result"));
    StepVerifier.create(proxyService.get(key))
        .expectNext("Result")
        .verifyComplete();
    verify(redisWrapper, times(0)).get(anyString());
  }

  @Test
  public void get_notFoundInCache_foundInRedis_cacheUpdated()
  {
    final var key = UUID.randomUUID().toString();
    when(localCache.get(key)).thenReturn(Optional.empty());
    when(redisWrapper.get(key)).thenReturn(Mono.just("ResultRedis"));

    StepVerifier.create(proxyService.get(key))
        .expectNext("ResultRedis")
        .verifyComplete();
    verify(localCache, times(1)).put(key, "ResultRedis");
  }

  @Test
  public void get_notFoundInRedis_returnEmpty()
  {
    final var key = UUID.randomUUID().toString();
    when(localCache.get(key)).thenReturn(Optional.empty());
    when(redisWrapper.get(key)).thenReturn(Mono.empty());

    StepVerifier.create(proxyService.get(key))
        .expectNextCount(0)
        .verifyComplete();

    verify(localCache, times(1)).get(key);
    verify(redisWrapper, times(1)).get(key);
    verify(localCache, times(0)).put(anyString(), anyString());
  }
}
