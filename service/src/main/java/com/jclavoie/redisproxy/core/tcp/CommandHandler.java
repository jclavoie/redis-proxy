package com.jclavoie.redisproxy.core.tcp;

import reactor.core.publisher.Mono;

public interface CommandHandler
{
  Mono<String> handleCommand(final String command);
}
