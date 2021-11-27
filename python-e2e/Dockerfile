ARG PYTHON_VERSION=3.9.7-bullseye
FROM python:${PYTHON_VERSION}
ENV SERVICE_CACHE_SIZE=10
ENV SERVICE_CACHE_TTL=10
ENV REDIS_PORT=6379
ENV SERVICE_URL="http://redis-proxy:8080"
ENV REDIS_HOST="redis"
#RUN apt-get update && apt-get install -y \
 #   poetry

RUN pip install poetry
COPY pyproject.toml /usr/src/e2e/
COPY main.py /usr/src/e2e
COPY docker-entrypoint.sh /usr/src/e2e

WORKDIR /usr/src/e2e

RUN poetry install

ENTRYPOINT ["bash", "/usr/src/e2e/docker-entrypoint.sh" ]