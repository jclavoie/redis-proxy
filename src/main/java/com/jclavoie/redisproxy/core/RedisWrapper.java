package com.jclavoie.redisproxy.core;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RedisWrapper
{
  @Autowired
  private ReactiveRedisOperations<String, String> redisTemplate;

  @PostConstruct
  public void testConnection()
  {
    redisTemplate.opsForValue().set("test", "success")
        .flatMap(resp -> redisTemplate.opsForValue().get("test"))
        .subscribe(response -> log.info("response from redis : {}", response));
  }

  public Mono<String> get(final String key)
  {
    return redisTemplate.opsForValue().get(key);
  }
}
