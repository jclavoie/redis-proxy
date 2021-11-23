package com.jclavoie.redisproxy.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.jclavoie.redisproxy.core.ProxyService;

@CrossOrigin
@RestController
public class ProxyController
{
  @Autowired
  private ProxyService proxyService;
}
