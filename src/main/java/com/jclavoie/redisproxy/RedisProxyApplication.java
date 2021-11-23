package com.jclavoie.redisproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RedisProxyApplication
{
  public static void main(String[] args)
  {
    SpringApplication.run(RedisProxyApplication.class, args);
  }
}
