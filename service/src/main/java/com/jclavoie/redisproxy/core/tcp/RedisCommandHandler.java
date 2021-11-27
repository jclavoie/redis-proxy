package com.jclavoie.redisproxy.core.tcp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jclavoie.redisproxy.core.ProxyService;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
/**
 * A Simple Redis Command Handler that only handles a single GET and return an echo on the HELLO
 */
public class RedisCommandHandler implements CommandHandler
{
  @Autowired
  private ProxyService proxyService;

  public Mono<String> handleCommand(final String command)
  {
    /**
     * Keep it simple and stupid just for HELLO and GET
     */
    final var values = command.split("\r\n");
    if (values[2].equals("HELLO") && values.length >= 3)
    {
      return handleHello(values);
    }
    else if (values[2].equals("GET") && values.length >= 5)
    {
      return handleGet(values);
    }
    return handleError("Not Implemented");
  }

  private Mono<String> handleHello(final String[] command)
  {
    var response = new String();
    for (String part : command)
    {
      response = response + part + "\r\n";
    }
    return Mono.just(response);
  }

  private Mono<String> handleGet(final String[] command)
  {
    final var requestedKey = command[4];
    return proxyService
        .get(requestedKey)
        .map(value ->
        {
          return "+%s\r\n".formatted(value);
        })
        .switchIfEmpty(Mono.just("$-1\r\n"));
  }

  private Mono<String> handleError(final String message)
  {
    final var error = "-Error %s\r\n".formatted(message);
    return Mono.just(error);
  }

}
