TAG = 0.0.1-SNAPSHOT
E2E_IMAGE = jclavoie/python-e2e
cache_size?=50
cache_ttl?=20

build:
	@docker build -t ${E2E_IMAGE}:${TAG} .

run:
	@docker run --network=redis-proxy-network  -e SERVICE_CACHE_SIZE=${cache_size} -e SERVICE_CACHE_TTL=${cache_ttl} ${E2E_IMAGE}:${TAG}
