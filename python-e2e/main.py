import argparse
import os
from collections import OrderedDict
from time import sleep

import redis
import requests
import requests_threads

async_session: requests_threads.AsyncSession
redis_write: redis.Redis
config = None


def _fill_redis(size):
    print("### Fill %s entries in Redis ###" % size)
    entries = OrderedDict()
    for i in range(size):
        entries[str(i)] = "value_" + str(i)
        redis_write.set(str(i), "value_" + str(i))
    return entries


def _empty_redis():
    redis_write.flushall()
    print("# Emptied REDIS #")


async def _get_value_from_http_parallel(keys: OrderedDict):
    rs = {}
    for key, _ in keys.items():
        rs[key] = await async_session.get(config.service_url + '/cache/' + key)
    return rs


async def _validate_all_items_gettable(entries: OrderedDict):
    success = True
    responses = await _get_value_from_http_parallel(entries)
    for k, response in responses.items():
        if not response.status_code or response.text != str(entries[k]):
            print("### TEST FAILED! Expected %s for key %s but go %s" % (entries[k], k, response))
            success = False
    return success


async def _validate_all_items_absent(entries: OrderedDict):
    success = True
    responses = await _get_value_from_http_parallel(entries)
    found = []
    for k, response in responses.items():
        if response.status_code != 404:
            found.append(k)
            success = False
    print("Found %i items in cache" % len(found))
    return success


async def test_get_entries_from_http(entries: OrderedDict):
    print("### Testing all entries are gettable from http ###")
    success = await _validate_all_items_gettable(entries)
    print("### TEST RESULT : %s" % str(success))
    return success


async def test_get_entries_from_http_after_redis_emptied(entries: OrderedDict):
    print("### Testing all entries deleted from REDIS still in Cache ###")
    success = await _validate_all_items_gettable(entries)
    print("### TEST RESULT : %s" % str(success))
    return success


async def test_wait_for_ttl_then_cache_empty(entries: OrderedDict, ttl):
    # Here I'm not testing that the entries expire exactly at ttl but just that the feature works
    # as it's just sanity check E2E and not behavior unit test
    print("### Testing care emptied after ttl ###")
    elapsed = 0
    result = False
    while elapsed <= ttl + 5 and not result:
        print("TTL is %s and time elapsed %s" % (str(ttl), str(elapsed)))
        result = await _validate_all_items_absent(entries)
        if result:
            break
        sleep(5)
        elapsed = elapsed + 5

    print("### TEST RESULT : %s" % str(result))
    return result


def __init__():
    global config, redis_write, async_session
    parser = argparse.ArgumentParser()
    parser.add_argument('--service-url',
                        default=os.environ.get('SERVICE_URL', "http://localhost:8080"))
    parser.add_argument('--redis-host',
                        default=os.environ.get('REDIS_HOST', "localhost"))
    parser.add_argument('--redis-port',
                        default=os.environ.get('REDIS_PORT', "6379"))
    parser.add_argument('--service-cache-size',
                        default=os.environ.get('SERVICE_CACHE_SIZE', 20))
    parser.add_argument('--service-cache-ttl',
                        default=os.environ.get('SERVICE_CACHE_TTL', 10))

    config = parser.parse_args()
    print(config)
    redis_write = redis.Redis(host=config.redis_host, port=config.redis_port, db=0)
    async_session = requests_threads.AsyncSession(int(config.service_cache_size))


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
            print("Failed to ping service, waiting %s" % str(ex))
            sleep(1)


async def main():
    print("### Service Ready! ###")
    entries = _fill_redis(int(config.service_cache_size))
    if not await test_get_entries_from_http(entries):
        print("Test Failed!")
        exit(-1)
    _empty_redis()
    if not await test_get_entries_from_http_after_redis_emptied(entries):
        print("Test Failed!")
        exit(-1)
    if not await test_wait_for_ttl_then_cache_empty(entries, int(config.service_cache_ttl)):
        print("Test Failed!")
        exit(-1)
    exit(0)


if __name__ == '__main__':
    __init__()
    print("### Init by pinging service until it's ready ###")
    _wait_for_service_ready()
    async_session.run(main)
