ARG PYTHON_VERSION=3.9.7-bullseye
FROM python:${PYTHON_VERSION}

#RUN apt-get update && apt-get install -y \
 #   poetry

RUN pip install poetry
COPY pyproject.toml /usr/src/e2e/
COPY main.py /usr/src/e2e
COPY docker-entrypoint.sh /usr/src/e2e

WORKDIR /usr/src/e2e

RUN poetry install

ENTRYPOINT [ "/usr/src/e2e/docker-entrypoint.sh" ]