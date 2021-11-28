TAG = 0.0.1-SNAPSHOT
E2E_IMAGE = jclavoie/python-e2e

build:
	@docker build -t ${E2E_IMAGE}:${TAG} .

run:
	@docker run --rm --network=redis-proxy-network \
 	-e SERVICE_NAME=${proxy_service_name} \
 	-e SERVICE_PORT=${proxy_service_http_port} \
 	-e SERVICE_TCP_PORT=${proxy_service_tcp_port} \
 	-e SERVICE_CACHE_SIZE=${proxy_service_cache_size} \
 	-e SERVICE_CACHE_TTL=${proxy_service_cache_ttl} \
 	-e SERVICE_MAX_REQUESTS=${proxy_service_max_concurrent_requests} \
 	-e REDIS_SERVICE_NAME=${redis_service_name} \
    -e REDIS_SERVICE_PORT=${redis_service_port} \
 	 ${E2E_IMAGE}:${TAG}
