package com.jclavoie.redisproxy.core;

import reactor.core.publisher.Mono;

public class RedisWrapper
{
  public Mono<String> get(final String key)
  {
    return Mono.just("Not Implemented");
  }
}
