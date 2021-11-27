package com.jclavoie.redisproxy.core.tcp;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.jclavoie.redisproxy.core.ProxyService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringJUnitConfig
@Slf4j
public class RedisTcpServerTest
{

  private static BeanTcpServer server;
  private static ProxyService proxyService;
  private static ReactiveRedisOperations<String, String> redis;

  @BeforeAll
  public static void init()
  {
    proxyService = mock(ProxyService.class);
    server = BeanTcpServer.builder().hostname("localhost").port(0).handler(
        new RedisCommandHandler(proxyService)).build();
    server.init();
    redis = redisOperations(server.getPort());
  }

  @BeforeEach
  public void beforeEach()
  {
    reset(proxyService);
  }

  @Test
  @SneakyThrows
  public void send_getRequest_valueFound_expectSuccess()
  {
    final var key = UUID.randomUUID().toString();
    final var value = UUID.randomUUID().toString();
    when(proxyService.get(key)).thenReturn(Mono.just(value));
    final var mono = redis.opsForValue().get(key);
    StepVerifier.create(mono)
        .expectNext(value)
        .verifyComplete();
  }

  @Test
  @SneakyThrows
  public void send_getRequest_valueNotFound_expectEmpty()
  {
    final var key = UUID.randomUUID().toString();
    when(proxyService.get(key)).thenReturn(Mono.empty());
    final var mono = redis.opsForValue().get(key);
    StepVerifier.create(mono)
        .expectNextCount(0)
        .verifyComplete();
  }

  private static ReactiveRedisOperations<String, String> redisOperations(final int port)
  {
    final var factory = new LettuceConnectionFactory("localhost", port);
    factory.afterPropertiesSet();
    return new ReactiveRedisTemplate(factory,
        RedisSerializationContext.string());
  }

}
