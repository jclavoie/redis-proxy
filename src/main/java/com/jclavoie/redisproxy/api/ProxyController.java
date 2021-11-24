package com.jclavoie.redisproxy.api;

import static com.jclavoie.redisproxy.api.ProxyController.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jclavoie.redisproxy.core.ProxyService;

import reactor.core.publisher.Mono;

@CrossOrigin
@RestController
@RequestMapping(PROXY_ROUTE)
public class ProxyController
{
  public static final String PROXY_ROUTE = "/v1/cache";
  @Autowired
  protected ProxyService proxyService;

  @GetMapping(
      value = "/{key}")
  public Mono<ResponseEntity<String>> getValue(@PathVariable("key") final String key)
  {
    return proxyService.get(key)
        .map(result -> ResponseEntity.ok(result))
        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
  }
}
