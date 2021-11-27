package com.jclavoie.redisproxy.api;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;

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
    return proxyService.get(key)
        .map(result -> ResponseEntity.ok(result))
        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
        /* I'm not sure entirely sure this works like I thought it would.
           I can easily test it with junit by forcing the `tryAcquire` to fail
           but I wasn't able to trigger during normal operaiton, even with a limit of 1 request
         */
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
