export cache_size?=200
export cache_ttl?=10
export max_concurrent_requests?=200

build-service:
	cd service && make build

build-e2e:
	cd python-e2e && make build

build: build-service build-e2e

compose-up:
	cache_size=${cache_size} cache_ttl=${cache_ttl} max_concurrent_requests=${max_concurrent_requests} docker-compose up -d

test: build compose-up test-run compose-down
test-no-build: compose-up test-run compose-down
test-run:
	cd python-e2e && $(MAKE) run

compose-down:
	docker-compose down