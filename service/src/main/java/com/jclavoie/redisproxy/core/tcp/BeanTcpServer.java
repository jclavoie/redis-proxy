package com.jclavoie.redisproxy.core.tcp;

import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

@Component
@Slf4j
public class BeanTcpServer
{
  @Value("${application.redis-proxy.tcp.hostname:localhost}")
  private final String hostname;
  @Value("${application.redis-proxy.tcp.port:6379}")
  @Getter
  private int port;
  private DisposableServer server;

  public BeanTcpServer(final String hostname, final int port)
  {
    this.hostname = hostname;
    this.port = port;
  }

  @PostConstruct
  public void init()
  {
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
                  .map(val -> Unpooled.copiedBuffer(val.getBytes(StandardCharsets.UTF_8))));
        }).bindNow();

    if (port == 0)
    {
      port = server.port();
    }
  }

  @PreDestroy
  public void destroy()
  {
    server.dispose();
  }
}
