* [High Level Architecture](#high-level-architecture)
* [What the code does](#what-the-code-does)
    * [Redis Proxy](#redis-proxy)
    * [E2E](#e2e)
* [How to run](#how-to-run)
* [How long did you spent on each part of the project](#how-long-did-you-spent-on-each-part-of-the-project)
* [Notes about requirements](#notes-about-requirements)
    * [Sequential processing of the request](#sequential-processing-of-the-request)
    * [Limiting the number of concurrent requests](#limiting-the-number-of-concurrent-requests)
    * [LRU Caching](#lru-caching)
* [Future Improvements and limitations](#future-improvements-and-limitations)
    * [Fully Blackbox solution](#fully-blackbox-solution)
    * [REDIS Authentication](#redis-authentication)

## High Level Architecture

![Alt text](proxy-cache-diagram.svg?raw=true )

## What the code does

### Redis Proxy

Simple Springboot + WebFlux app that does the following

1. Instantiate a REDIS client to the backing redis & a local LRU cache with an expiry ttl
2. Instantiate the `ProxyService` class that serves a `get` method that will try to get the `key`
   from the local cache or from REDIS if it's not found. The local cache is updated after a get from
   REDIS
3. Instantiate an HTTP controller on port 8080
    1. The HTTP controller serves 2 endpoints
        1. A simple `/health` that returns `200` used to test if the service is up
        2. The `/cache/${key}` endpoint will call the `get` method from the ProxyService
4. Instantiate a TCP Server on REDIS port 6379
    1. The TCP server listens for the `HELLO` and `GET` Redis command
        1. `HELLO` will simple echo back what it receives
        2. `GET` will call the `get` method from the Proxy Service
        3. Returns ERROR or anything else

### E2E

Simple python running that does the following :

1. Loads up the config from ENV variables
2. Ping the `/health` http endpoint until the service is up and running
3. Execute the following tests:
    1. Fill in the REDIS instance
    2. Test fetch all entries from HTTP
    3. Empty the REDIS instance
    4. Test fetch all entries from HTTP (still available in cache)
    5. Test entries are not fetchable after expiry
    6. Test connect to the Redis-Proxy using TCP and do a REDIS GET query from it.

## How to run

The root-level Makefile supports the following commands:

* **test**: Build everything, scale up the compose project & run E2E
* **test-no-build**: Scale up the compose project & run E2E
* **test-run**: Only run the E2E
* **build-service**: Builds the Redis-Proxy application
* **build-e2e**: Builds the E2E project
* **build**: Builds both the Application & the E2E
* **compose-up** and **compose-down**: Scale up/down the docker-compose project with the Application

So simply running the `make test` command will take of everything

The following parameters are configurable in the Makefile and propagates across services :

* proxy_service_cache_size = 100 (max size of local cache)
* proxy_service_cache_ttl = 10 (expiry time in second of local cache)
* proxy_service_max_concurrent_requests = 100 (max requests allowed by the service)
* proxy_service_name = redis-proxy (dns-name of the proxy service)
* proxy_service_http_port = 8080 (http port of the proxy service)
* proxy_service_tcp_port = 6379 (redis port of the proxy service)
* proxy_service_tcp_hostname = 0.0.0.0 (address of the interface the TCP server listens from)
* redis_service_name = redis (dns-name of the redis instance)

## How long did you spent on each part of the project

* Basic Setup (git setup, ide setup, Spring base project + dockerfile/compose + makefile) : 1h 45
* Java implementation 1st run (Basic Java App with cache only , working in dockercompose) : 2h 45
* Integrate Redis in code + make it work in docker compose : 1h
* Python E2E simple set/get + its integration in a docker + test against the docker-compose : 1h30
* Add Suite of E2E + implement cache expiry : 1h
* Rework E2E for cache ttl test + wrap tests & app config with env variables : 1h30
* Rework E2E do make http requests in parallel : 30m
* Implement concurrent request limiter. 2h but I spent most of time trying to trigger the error by
  the E2E, which I wasn't able to
* Implement simple REDIS protocol on TCP: 1h30
* Figuring out that a docker container needs to listen to 0.0.0.0 and not localhost : a few hours
* Cleanup makefile & code a bit : 1h
* Add doc : 2h

## Notes about requirements

### Sequential processing of the request

Because of the nature of Project Reactor & Spring WebFlux, I went directly with concurrent
processing of the HTTP requests. The Lettuce Redis client handles concurrent requests.

### Limiting the number of concurrent requests

Again, due to the nature of Reactor, this one is a bit harder to correctly test. I used
the `Bulkhead`  strategy to limit the number of requests that can access the cache at the same
time (and returns `429` if it can't get an instant access). I did write an E2E test for that
requirement but I chose not to run it because it can be extremly flaky as it relies on the test
setup being able to push requests _faster_ than what the app can process. The case is tested in unit
tests though.

### LRU Caching

To meet the LRU requirements I used a simple fixed-size LinkedHashMap combined with another map
holding the expiry time of each keys. To handle concurrency, write operation is protected by a mutex
by the `synchronized` keyword. The LinkedHashMap has both space & time complexity of O(1). In
Real-World situation I would tend to use an off the shelve caching solution rather than
re-implementing but I wasn't getting deterministic test result using `Guava` and `Caffeine` does not
use the LRU algorithm.

Weither the off-the-shelves or custom solution, they all have a small issue when used in a Reactive
flow though:
In order to handle concurrency, the write operations are protected by a mutex which is blocking.
Ideally we want to avoid any blocks in a reactive chain but, as of today, I'm not aware of a
Reactive Cache in Java.

## Future Improvements and limitations

### Fully Blackbox solution

In order to make a fully blackbox solution. The compose would expose a single entry point that
routes requests to the good container. The solution could look like this:

1. The compose exposes an `HAProxy-Edge` service that routes requests
    1. HTTP requests to the RedisProxy service 8080 port
    2. TCP requests on 6379 routed to a `Redis Sentinel` instance that routes `READ` requests to the
       ProxyService and `WRITE` requests directly to REDIS
2. This Edge service would be the only one accessible from outside of the docker compose

### REDIS Authentication

The solution is currently configured using no Authentication. Ideally we'd set it up