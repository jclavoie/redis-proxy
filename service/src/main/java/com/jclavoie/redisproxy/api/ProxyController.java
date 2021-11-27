package com.jclavoie.redisproxy.api;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.jclavoie.redisproxy.core.ProxyService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@CrossOrigin
@RestController
@Slf4j
public class ProxyController
{
  @Autowired
  protected ProxyService proxyService;

  @Autowired
  protected Bulkhead bulkhead;

  @GetMapping(
      value = "/cache/{key}")
  public Mono<ResponseEntity<String>> getValue(@PathVariable("key") final String key)
  {
    log.info("Bulkhead capacity {} and free {}",
        bulkhead.getMetrics().getMaxAllowedConcurrentCalls(),
        bulkhead.getMetrics().getAvailableConcurrentCalls());
    return proxyService.get(key)
        .delayElement(Duration.ofMillis(1000))
        .elapsed()
        .map(result -> ResponseEntity.ok(result.getT2()))
        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
        .transformDeferred(BulkheadOperator.of(bulkhead))
        .onErrorResume(err ->
        {
          log.error("Request failed with : {}", err.getMessage());
          if (err instanceof BulkheadFullException)
          {
            return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
          }
          else
          {
            return Mono.just(ResponseEntity.internalServerError().build());
          }
        });
  }

  @GetMapping(value = "/health")
  public ResponseEntity getStatus()
  {
    return ResponseEntity.ok().build();
  }
}
