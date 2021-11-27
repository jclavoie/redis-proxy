TAG = 0.0.1-SNAPSHOT
E2E_IMAGE = jclavoie/python-e2e
cache_size?=50
cache_ttl?=20
max_concurrent_requests?=1

build:
	@docker build -t ${E2E_IMAGE}:${TAG} .

run:
	@docker run --rm --network=redis-proxy-network  -e SERVICE_CACHE_SIZE=${cache_size} -e SERVICE_CACHE_TTL=${cache_ttl} \
 	-e MAX_CONCURRENT_REQUESTS=${max_concurrent_requests} \
 	-e  SERVICE_NAME=${proxy_service_name} -e SERVICE_PORT=${proxy_service_http_port} \
 	-e SERVICE_TCP_PORT=${tcp_server_port} \
 	 ${E2E_IMAGE}:${TAG}
