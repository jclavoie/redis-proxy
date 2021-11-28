import argparse
import os
import uuid
from collections import OrderedDict
from time import sleep

import redis
import requests
import requests_threads

async_session: requests_threads.AsyncSession
redis_write: redis.Redis
redis_read: redis.Redis
config = None
service_http_url: str


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


async def _get_value_from_http_parallel(keys: OrderedDict,
                                        session: requests_threads.AsyncSession):
    promises = {}
    responses = {}
    for key, _ in keys.items():
        promises[key] = session.get(service_http_url + '/cache/' + key)
    for key, promise in promises.items():
        responses[key] = await promise
    return responses


async def _validate_all_items_gettable(entries: OrderedDict):
    success = True
    responses = await _get_value_from_http_parallel(entries, async_session)
    for k, response in responses.items():
        if not response.status_code or response.text != str(entries[k]):
            print("### TEST FAILED! Expected %s for key %s but got %s" % (entries[k], k, response))
            success = False
    return success


async def _validate_all_items_absent(entries: OrderedDict):
    success = True
    responses = await _get_value_from_http_parallel(entries, async_session)
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
    print("### Testing cache emptied after ttl ###")
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


def test_get_from_redis_tcp_proxy():
    print("### Testing cache using REDIS-PROXY TCP ###")
    key = str(uuid.uuid4())
    value = str(uuid.uuid4())
    redis_write.set(key, value)
    value_from_cache = redis_read.get(key).decode('utf-8')
    if value_from_cache != value:
        print("Expected %s but got %s" % (value, value_from_cache))
        return False
    redis_write.delete(key)
    value_from_cache_after_delete = redis_read.get(key).decode('utf-8')
    if value_from_cache_after_delete != value:
        print("Expected %s but got %s" % (value, value_from_cache_after_delete))
        return False
    print("### TEST RESULT : True")
    return True


async def test_bust_concurrent_request_limit():
    print("### Testing busting concurrent limit gives 429 ###")
    target = int(config.service_max_requests) * 4
    success = False
    entries = _fill_redis(target)
    responses = await _get_value_from_http_parallel(entries, requests_threads.AsyncSession(target))
    for k, response in responses.items():
        if response.status_code == 429 and not success:
            print("### TEST SUCCESS! 429 received")
            success = True
    return success


def __init__():
    global config, redis_write, redis_read, async_session, service_http_url
    parser = argparse.ArgumentParser()
    parser.add_argument('--service-name',
                        default=os.environ.get('SERVICE_NAME', "localhost"))
    parser.add_argument('--service-port',
                        default=os.environ.get('SERVICE_PORT', 18080))
    parser.add_argument('--service-tcp-port',
                        default=os.environ.get('SERVICE_TCP_PORT', 16379))
    parser.add_argument('--service-cache-size',
                        default=os.environ.get('SERVICE_CACHE_SIZE', 20))
    parser.add_argument('--service-cache-ttl',
                        default=os.environ.get('SERVICE_CACHE_TTL', 5))
    parser.add_argument('--service-max-requests',
                        default=os.environ.get('SERVICE_MAX_REQUESTS', 5))
    parser.add_argument('--redis-host',
                        default=os.environ.get('REDIS_SERVICE_NAME', "localhost"))
    parser.add_argument('--redis-port',
                        default=os.environ.get('REDIS_SERVICE_PORT', 6379))

    config = parser.parse_args()
    print(config)
    service_http_url = "http://" + config.service_name + ":" + str(config.service_port)
    redis_write = redis.Redis(host=config.redis_host, port=config.redis_port, db=0)
    redis_read = redis.Redis(host=config.service_name, port=config.service_tcp_port)
    async_session = requests_threads.AsyncSession(int(config.service_max_requests))


def _wait_for_service_ready():
    success = False
    while not success:
        try:
            resp = requests.get(service_http_url + '/health')
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
    # Not executing this test because it's flaky as we'd need a very low max number of
    # concurrent requests and a high number of actually concurrent one
    # if not await test_bust_concurrent_request_limit():
    #    print("Test Failed!")
    #    exit(-1)
    if not test_get_from_redis_tcp_proxy():
        print("Test Failed!")
        exit(-1)
    exit(0)


if __name__ == '__main__':
    __init__()
    print("### Init by pinging service until it's ready ###")
    _wait_for_service_ready()
    async_session.run(main)
