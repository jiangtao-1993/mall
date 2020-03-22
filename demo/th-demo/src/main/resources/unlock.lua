if (redis.call('HEXISTS', KEYS[1], ARGV[1]) == 0) then
    return nil;
end;
local count = redis.call('HINCRBY', KEYS[1], ARGV[1], -1);
if (count > 0) then
    redis.call('EXPIRE', KEYS[1], ARGV[2]);
    return nil;
else
    redis.call('DEL', KEYS[1]);
    return nil;
end;