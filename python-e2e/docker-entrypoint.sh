#!/bin/bash

export SERVICE_URL="http://redis-proxy:8080/v1/cache"
export REDIS_HOST="redis"
export REDIS_PORT=6379

poetry run python main.py
