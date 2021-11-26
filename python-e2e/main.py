import argparse
import os
from collections import OrderedDict
from time import sleep

import busypie
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
    print("# Emptied REDIS #")


def _get_value_from_http(key):
    return requests.get(config.service_url + '/cache/' + key)


def _validate_all_items_gettable(entries: OrderedDict):
    success = True
    for k, v in entries.items():
        response = _get_value_from_http(k)
        if not response.status_code or response.text != str(v):
            print("### TEST FAILED! Expected ", v, " for key ", k, "but got ", response.text)
            success = False
    return success


def _validate_all_items_absent(entries: OrderedDict):
    success = True
    found = []
    for k, v in entries.items():
        response = _get_value_from_http(k)
        if response.status_code != 404:
            found.append(k)
            success = False
    print("Found %i items in cache", len(found))
    return success


def test_get_entries_from_http(entries: OrderedDict):
    print("### Testing all entries are gettable from http ###")
    success = _validate_all_items_gettable(entries)
    print("### TEST RESULT : %s" % str(success))
    return success


def test_get_entries_from_http_after_redis_emptied(entries: OrderedDict):
    print("### Testing all entries deleted from REDIS still in Cache ###")
    success = _validate_all_items_gettable(entries)
    print("### TEST RESULT : %s" % str(success))
    return success


def test_wait_for_ttl_then_cache_empty(entries: OrderedDict, ttl):
    print("### Testing all entries deleted from REDIS still in Cache ###")
    result = busypie.wait_at_most(int(ttl) + 10).poll_interval(busypie.FIVE_SECONDS).until(
        lambda: _validate_all_items_absent(entries))
    print("### TEST RESULT : %s" % str(result))
    return True


def __init__():
    global config, redis_write
    parser = argparse.ArgumentParser()
    parser.add_argument('--service-url',
                        default=os.environ.get('SERVICE_URL', "http://localhost:8080"))
    parser.add_argument('--redis-host',
                        default=os.environ.get('REDIS_HOST', "localhost"))
    parser.add_argument('--redis-port',
                        default=os.environ.get('REDIS_PORT', "6379"))
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
    print("### Init by pinging service until it's ready ###")
    _wait_for_service_ready()
    print("### Service Ready! ###")
    entries = _fill_redis(50)
    test_get_entries_from_http(entries)
    _empty_redis()
    test_get_entries_from_http_after_redis_emptied(entries)
    test_wait_for_ttl_then_cache_empty(entries, config.service_cache_ttl)
    exit(0)


main()
