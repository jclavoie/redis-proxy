package com.jclavoie.redisproxy.core.tcp;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

@Component
@RequiredArgsConstructor
public class BeanTcpServer
{
  @Value("${application.redis-proxy.tcp.hostname:localhost}")
  private final String hostname;
  @Value("${application.redis-proxy.tcp.port:6379}")
  private final int port;
  private DisposableServer server;

  @PostConstruct
  public void init()
  {
    TcpServer
        .create()
        .host(hostname)
        .port(port)
        .handle((inbound, outbound) ->
        {
          return outbound.send(inbound.receive().retain());
        }).bindNow();
  }

  @PreDestroy
  public void destroy()
  {
    server.dispose();
  }
}
