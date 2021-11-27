package com.jclavoie.redisproxy.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.jclavoie.redisproxy.core.ProxyService;

import reactor.core.publisher.Mono;

@SpringJUnitWebConfig
@WebFluxTest(ProxyController.class)
public class ProxyControllerTest
{
  @Autowired
  WebTestClient webTestClient;

  @MockBean(answer = Answers.CALLS_REAL_METHODS)
  @Autowired
  ProxyController proxyController;

  @MockBean
  @Autowired
  ProxyService proxyService;

  Bulkhead bulkhead;

  @BeforeEach
  public void init()
  {
    // that requires manual auto-wiring. That's why some beans are Autowired
    // + MockBeans
    final var config =
        BulkheadConfig.custom()
            .maxConcurrentCalls(1)
            .maxWaitDuration(Duration.ZERO)
            .build();
    final var registry = BulkheadRegistry.of(config);
    proxyController.bulkhead = spy(registry.bulkhead("concurrent_get"));
    bulkhead = proxyController.bulkhead;
    proxyController.proxyService = proxyService;
  }

  @Test
  public void getItem_isFound_itemReturned()
  {
    final var key = "MyKey";
    final var expectedResult = "Result";
    when(proxyService.get(key)).thenReturn(Mono.just(expectedResult));

    webTestClient
        .get()
        .uri(builder -> builder.path("/cache/{key}").build(key))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(String.class)
        .isEqualTo(expectedResult);
  }

  @Test
  public void getItem_notFound_expect404()
  {
    final var key = "MyKey";
    when(proxyService.get(key)).thenReturn(Mono.empty());

    webTestClient
        .get()
        .uri(builder -> builder.path("/cache/{key}").build(key))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  public void getItem_bustBulkhead_expect429()
  {
    final var wasCalled = new AtomicBoolean(false);
    final var key = "MyKey";
    when(bulkhead.tryAcquirePermission()).thenReturn(false);
    when(proxyService.get(key)).thenReturn(
        Mono.empty().map(__ ->
        {
          wasCalled.set(true);
          return "hello";
        }));
    webTestClient
        .get()
        .uri(builder -> builder.path("/cache/{key}").build(key))
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

    assertFalse(wasCalled.get());
  }
}
