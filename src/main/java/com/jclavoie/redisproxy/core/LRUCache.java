package com.jclavoie.redisproxy.core;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LRUCache extends LinkedHashMap
{
  private int capacity;

  public LRUCache(final int capacity)
  {
    super(capacity, 0.75f, true);
    this.capacity = capacity;
  }

  @Override
  protected boolean removeEldestEntry(final Entry eldest)
  {
    final var isOver = this.size() > capacity;
    if (isOver)
    {
      log.info("Cache size is {}, evicting oldest entry", this.size());
    }
    return isOver;
  }
}
