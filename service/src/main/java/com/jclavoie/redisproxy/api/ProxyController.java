package com.jclavoie.redisproxy.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.jclavoie.redisproxy.core.ProxyService;

import reactor.core.publisher.Mono;

@CrossOrigin
@RestController
public class ProxyController
{
  @Autowired
  protected ProxyService proxyService;

  @GetMapping(
      value = "/cache/{key}")
  public Mono<ResponseEntity<String>> getValue(@PathVariable("key") final String key)
  {
    return proxyService.get(key)
        .map(result -> ResponseEntity.ok(result))
        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
  }

  @GetMapping(value = "/health")
  public ResponseEntity getStatus()
  {
    return ResponseEntity.ok().build();
  }
}
