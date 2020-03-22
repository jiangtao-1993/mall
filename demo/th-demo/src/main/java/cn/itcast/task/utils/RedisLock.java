package cn.itcast.task.utils;


public interface RedisLock {
    boolean lock(long releaseTime);
    void unlock();
}
