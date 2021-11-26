export TAG = 0.0.1-SNAPSHOT
export RP_IMAGE = jclavoie/redis-proxy

build-service:
	cd service && make build

build-e2e:
	cd python-e2e && make build

build: build-service build-e2e

compose-up:
	docker-compose up -d
	#@echo "Pausing for 5 seconds so it's fully initiated"
	#@sleep 5

test: build compose-up
	cd python-e2e && make run
	make compose-down

test-no-build: compose-up
	cd python-e2e && make run
	make compose-down

compose-down:
	docker-compose down