package com.jclavoie.redisproxy.core.tcp;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;
import reactor.test.StepVerifier;

@SpringJUnitConfig
@Slf4j
public class BeanTcpServerTest
{

  @Test
  @SneakyThrows
  public void test()
  {
    final var server = new BeanTcpServer("localhost", 6379);
    final var response = new AtomicReference<String>();
    final CountDownLatch latch = new CountDownLatch(1);
    server.init();
    final var client = TcpClient.create().host("localhost").port(6379)
        .handle((in, out) ->
        {
          in.receive().retain().asString().subscribe(resp ->
          {
            response.set(resp);
            latch.countDown();
          });
          return out.neverComplete();
        })
        .connectNow();
    client.outbound().sendString(Mono.just("Hello world!")).then().subscribe();
    latch.await(5, TimeUnit.SECONDS);
    assertEquals("Hello World!", response.get());
  }

  @Test
  @SneakyThrows
  public void send_command()
  {
    final var server = new BeanTcpServer("localhost", 0);
    server.init();
    var redis = redisOperations(server.getPort());
    var mono = redis.opsForValue().get("hello");
    StepVerifier.create(mono)
        .consumeNextWith(val -> log.info("{}", val))
        .verifyComplete();
  }

  private ReactiveRedisOperations<String, String> redisOperations(final int port)
  {
    final var factory = new LettuceConnectionFactory("localhost", port);
    factory.afterPropertiesSet();
    return new ReactiveRedisTemplate(factory,
        RedisSerializationContext.string());
  }

}
