package com.jclavoie.redisproxy.core.cache;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RedisCache
{
  @Autowired
  private ReactiveRedisOperations<String, String> redisTemplate;

  @PostConstruct
  public void testConnection()
  {
    final var key = UUID.randomUUID().toString();
    redisTemplate.opsForValue().set(key, "success")
        .flatMap(resp -> redisTemplate.opsForValue().getAndDelete(key))
        .subscribe(response -> log.info("response from redis : {}", response));
  }

  public Mono<String> get(final String key)
  {
    return redisTemplate.opsForValue().get(key);
  }
}
