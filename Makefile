export TAG = 0.0.1-SNAPSHOT
export RP_IMAGE = jclavoie/redis-proxy

build:
	@docker build -t ${RP_IMAGE}:${TAG} --build-arg TAG=${TAG} .

compose-up:
	docker-compose up -d

compose-down:
	docker-compose down