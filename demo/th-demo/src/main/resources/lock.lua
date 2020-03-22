if (redis.call('EXISTS', KEYS[1]) == 0) then
    redis.call('HSET', KEYS[1], ARGV[1], 1);
    redis.call('EXPIRE', KEYS[1], ARGV[2]);
    return 1;
end;
if (redis.call('HEXISTS', KEYS[1], ARGV[1]) == 1) then
    redis.call('HINCRBY', KEYS[1], ARGV[1], 1);
    redis.call('EXPIRE', KEYS[1], ARGV[2]);
    return 1;
end;
return 0;