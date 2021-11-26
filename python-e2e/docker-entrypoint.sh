#!/bin/bash

export SERVICE_URL="http://redis-proxy:8080"
export REDIS_HOST="redis"
export REDIS_PORT=6379
export SERVICE_CACHE_SIZE=50
export SERVICE_CACHE_TTL=20

poetry run python main.py
