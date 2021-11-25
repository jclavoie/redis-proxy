import argparse
import os
from collections import OrderedDict
from time import sleep

import redis
import requests

redis_write: redis.Redis
config = None


def _fill_redis(size):
    print("### Fill %s entries in Redis ###")
    entries = OrderedDict()
    for i in range(size):
        entries[str(i)] = "value_" + str(i)
        redis_write.set(str(i), "value_" + str(i))
    return entries


def _empty_redis():
    redis_write.flushall()


def test_get_entries_from_http(entries: OrderedDict):
    print("### Testing all entries are gettable from http ###")
    failed = False
    for k, v in entries.items():
        response = requests.get(config.service_url + '/cache/' + k)
        if response.text != str(v):
            print("### TEST FAILED! Expected ", v, " for key ", k, "but got ", response.text)
            failed = True
    print("### TEST RESULT : %s" % str(not failed))


def test_delete_from_redis_still_in_http_cache(entries: OrderedDict):
    print("### Testing all entries deleted from REDIS still in Cache ###")
    _empty_redis()
    failed = False
    for k, v in entries.items():
        response = requests.get(config.service_url + '/cache/' + k)
        if response.text != str(v):
            print("### TEST FAILED! Expected ", v, " for key ", k, "but got ", response.text)
            failed = True
    print("### TEST RESULT : %s" % str(not failed))


def test_wait_for_ttl_then_cache_empty(entries: OrderedDict):
    return


def __init__():
    global config, redis_write
    parser = argparse.ArgumentParser()
    parser.add_argument('--service-url',
                        default=os.environ.get('SERVICE_URL', "http://localhost:18080/v1/cache"))
    parser.add_argument('--redis-host',
                        default=os.environ.get('REDIS_HOST', "localhost"))
    parser.add_argument('--redis-port',
                        default=os.environ.get('REDIS_PORT', "16379"))
    parser.add_argument('--service-cache-size',
                        default=os.environ.get('SERVICE_CACHE_SIZE', 50))
    parser.add_argument('--service-cache-ttl',
                        default=os.environ.get('SERVICE_CACHE_TTL', 60))

    config = parser.parse_args()
    print(config)
    redis_write = redis.Redis(host=config.redis_host, port=config.redis_port, db=0)


def _wait_for_service_ready():
    success = False
    while not success:
        try:
            resp = requests.get(config.service_url + '/health')
            if resp.status_code == 200:
                success = True
            else:
                sleep(1)
        except Exception as ex:
            print("Failed to ping service, waiting %s", str(ex))
            sleep(1)


def main():
    __init__()
    print('hello world')
    _wait_for_service_ready()
    print("Service online!")
    entries = _fill_redis(50)
    test_get_entries_from_http(entries)
    test_delete_from_redis_still_in_http_cache(entries)
    test_wait_for_ttl_then_cache_empty()
    exit(0)


main()
