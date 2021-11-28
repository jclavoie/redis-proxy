# redis-proxy

## About Requirements and Design choices

* The requirements MVP asked for a sequential processing of the requests and then concurrent
  processing in the "bonus" section. Because of the reactive nature of Redis and the type of
  operation, I chose to implement the solution using Spring WebFlux. Because the entire flow is
  non-blocking

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