package com.jclavoie.redisproxy;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.event.EventListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
public class Main
{
  public static void main(String[] args)
  {
    try
    {
      new SpringApplicationBuilder(Main.class)
          .web(WebApplicationType.REACTIVE)
          .run(args);
    }
    catch (final Exception e)
    {
      log.error("An error occurred with the application with the following stack trace:", e);
      System.exit(1);
    }
  }

  @EventListener(ApplicationReadyEvent.class)
  public void doSomethingAfterStartup()
  {
    log.info("Service Started!");
  }
}
