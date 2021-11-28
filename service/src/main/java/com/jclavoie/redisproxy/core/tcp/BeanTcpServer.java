package com.jclavoie.redisproxy.core.tcp;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpServer;

//@Controller
@Slf4j
@Builder
public class BeanTcpServer
{
  private final String hostname;
  private final CommandHandler handler;
  @Getter
  private int port;
  private DisposableServer server;
  
  @PostConstruct
  public void init()
  {
    log.info("Initialing TCP server on port {}", port);
    server = TcpServer
        .create()
        .host(hostname)
        .port(port)
        .handle((inbound, outbound) ->
        {
          return outbound.send(
              inbound.receive()
                  .retain()
                  .asString()
                  .flatMap(val -> ByteBufMono.fromString(handler.handleCommand(val))));
        }).bindNow();

    if (port == 0)
    {
      port = server.port();
    }

    final var client = TcpClient.create()
        .host("localhost")
        .port(port)
        .connectNow()
        .bind();
    client.outbound().sendString(Mono.just("hello")).then().block();
  }

  @PreDestroy
  public void destroy()
  {
    server.dispose();
  }
}
