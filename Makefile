export TAG = 0.0.1-SNAPSHOT
export RP_IMAGE = jclavoie/redis-proxy

build-service:
	cd service && make build

compose-up:
	docker-compose up -d

compose-down:
	docker-compose down