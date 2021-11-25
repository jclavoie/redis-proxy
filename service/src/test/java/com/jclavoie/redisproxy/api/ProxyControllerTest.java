package com.jclavoie.redisproxy.api;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

  @BeforeEach
  public void init()
  {
    // that requires manual auto-wiring. That's why some beans are Autowired
    // + MockBeans
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
}
