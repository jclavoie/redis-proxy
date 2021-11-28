export proxy_service_cache_size?=200
export proxy_service_cache_ttl?=10
export proxy_service_max_concurrent_requests?=200
export proxy_service_name?=redis-proxy
export proxy_service_http_port?=8080
export proxy_service_tcp_port?=6379
export proxy_service_tcp_hostname?=0.0.0.0
export redis_service_name?=redis
# This one we keep it fixed
export redis_service_port=6379

build-service:
	cd service && make build

build-e2e:
	cd python-e2e && make build

build: build-service build-e2e

compose-up:
	proxy_service_cache_size=${proxy_service_cache_size} \
 	proxy_service_cache_ttl=${proxy_service_cache_ttl} \
 	proxy_service_max_concurrent_requests=${proxy_service_max_concurrent_requests} \
 	proxy_service_http_port=${proxy_service_http_port} \
 	proxy_service_tcp_port=${proxy_service_tcp_port} \
 	proxy_service_tcp_hostname=${proxy_service_tcp_hostname} \
 	docker-compose up -d

test: build compose-up test-run compose-down
test-no-build: compose-up test-run compose-down

test-run:
	cd python-e2e && $(MAKE) run

compose-down:
	docker-compose down