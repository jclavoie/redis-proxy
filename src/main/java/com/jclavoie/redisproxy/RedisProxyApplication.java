package com.jclavoie.redisproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RedisProxyApplication
{
  public static void main(String[] args)
  {
    SpringApplication.run(RedisProxyApplication.class, args);
  }
}
