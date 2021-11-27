package com.jclavoie.redisproxy.core.tcp;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.ByteBufMono;
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
  private final CommandHandler handler;
  private DisposableServer server;

  @Autowired
  public BeanTcpServer(final String hostname, final int port, final CommandHandler commandHandler)
  {
    this.hostname = hostname;
    this.port = port;
    this.handler = commandHandler;
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
                  .flatMap(val -> ByteBufMono.fromString(handler.handleCommand(val))));
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
