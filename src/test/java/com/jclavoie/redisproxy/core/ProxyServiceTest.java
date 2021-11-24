package com.jclavoie.redisproxy.core;

import static org.mockito.Mockito.*;

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
    when(localCache.get("MyKey")).thenReturn(Mono.just("Result"));
    StepVerifier.create(proxyService.get("MyKey"))
        .expectNext("Result")
        .verifyComplete();
    verify(redisWrapper, times(0)).get(anyString());
  }

  @Test
  public void get_notFoundInCache_foundInRedis_cacheUpdated()
  {
    when(localCache.get("MyKey")).thenReturn(Mono.empty());
    when(redisWrapper.get("MyKey")).thenReturn(Mono.just("ResultRedis"));

    StepVerifier.create(proxyService.get("MyKey"))
        .expectNext("ResultRedis")
        .verifyComplete();
    verify(localCache, times(1)).put("MyKey", "ResultRedis");
  }

  @Test
  public void get_notFoundInRedis_returnEmpty()
  {
    when(localCache.get("MyKey")).thenReturn(Mono.empty());
    when(redisWrapper.get("MyKey")).thenReturn(Mono.empty());

    StepVerifier.create(proxyService.get("MyKey"))
        .expectNextCount(0)
        .verifyComplete();

    verify(localCache, times(1)).get("MyKey");
    verify(redisWrapper, times(1)).get("MyKey");
    verify(localCache, times(0)).put(anyString(), anyString());
  }
}
