package com.jclavoie.redisproxy.core.tcp;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;

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
}
