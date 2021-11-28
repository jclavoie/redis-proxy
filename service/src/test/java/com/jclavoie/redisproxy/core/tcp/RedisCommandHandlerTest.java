package com.jclavoie.redisproxy.core.tcp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.jclavoie.redisproxy.core.ProxyService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringJUnitConfig
public class RedisCommandHandlerTest
{
  private final ProxyService proxyService = mock(ProxyService.class);
  private final RedisCommandHandler redisCommandHandler = new RedisCommandHandler(proxyService);

  @BeforeEach
  public void init()
  {
    reset(proxyService);
  }

  @Test
  public void handleCommand_withHello_returnEcho()
  {
    final var hello = "*2\r\n$5\r\nHELLO\r\n$1\r\n3\r\n";
    StepVerifier.create(redisCommandHandler.handleCommand(hello))
        .expectNext(hello)
        .verifyComplete();
  }

  @Test
  public void handleCommand_withGet_returnFromProxy()
  {
    when(proxyService.get("KEY")).thenReturn(Mono.just("VALUE"));
    final var command = "*2\r\n$3\r\nGET\r\n$3\r\nKEY\r\n";
    final var expect = "+VALUE\r\n";
    StepVerifier.create(redisCommandHandler.handleCommand(command))
        .expectNext(expect)
        .verifyComplete();
  }

  @Test
  public void handleCommand_withNotImplemented_returnError()
  {
    final var command = "*2\r\n$3\r\nSET\r\n$3\r\nKEY\r\n";
    final var expect = "-Error";
    StepVerifier.create(redisCommandHandler.handleCommand(command))
        .consumeNextWith(resp -> assertTrue(resp.startsWith(expect)))
        .verifyComplete();
  }
}
