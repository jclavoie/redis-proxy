package com.jclavoie.redisproxy.core.config;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import lombok.extern.slf4j.Slf4j;

@org.springframework.context.annotation.Configuration
@Slf4j
public class ServerConfig
{
  @Bean
  public Bulkhead getRestThrottlingLimiter(
      @Value("${application.redis-proxy.requests.max_concurrent:100}") final int maxNbRequests)
  {
    log.info("Max concurrent requests is {}", maxNbRequests);
    final var config =
        BulkheadConfig.custom()
            .maxConcurrentCalls(maxNbRequests)
            .maxWaitDuration(Duration.ZERO)
            .build();
    final var registry = BulkheadRegistry.of(config);
    return registry.bulkhead("concurrent_get");
  }

  /*@Bean
  public BeanTcpServer getRedisTcpServer(
      @Value("${application.redis-proxy.tcp.hostname:localhost}") final String host,
      @Value("${application.redis-proxy.tcp.port:6379}") final int port,
      final RedisCommandHandler commandHandler)
  {
    return BeanTcpServer.builder().port(port).hostname(host).handler(commandHandler)
        .build();
  }*/
}
