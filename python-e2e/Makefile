export TAG = 0.0.1-SNAPSHOT
export E2E_IMAGE = jclavoie/python-e2e

build:
	@docker build -t ${E2E_IMAGE}:${TAG} .

run:
	@docker run --network=redis-proxy-network ${E2E_IMAGE}:${TAG}
