import argparse
import os

import redis
import requests

redis_write = None
config = None


def write_in_redis_get_from_service():
    redis_write.set("hello", "world")
    response = requests.get(config.service_url + '/hello')
    print(response.text)


def __init__():
    global config, redis_write
    parser = argparse.ArgumentParser()
    parser.add_argument('--service-url',
                        default=os.environ.get('SERVICE_URL', "http://localhost:18080/v1/cache"))
    parser.add_argument('--redis-host',
                        default=os.environ.get('REDIS_HOST', "localhost"))
    parser.add_argument('--redis-port',
                        default=os.environ.get('REDIS_PORT', "16379"))

    config = parser.parse_args()
    print(config)
    redis_write = redis.Redis(host=config.redis_host, port=config.redis_port, db=0)


def main():
    __init__()
    print('hello world')
    write_in_redis_get_from_service()


main()
