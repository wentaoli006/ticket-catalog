package com.owen.test.infrastructure.redis;

import com.owen.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;

/**
 * Redis 案例
 */
@Slf4j
@SpringBootTest
public class RedisTest {

    @Resource
    private IRedisService redissonService;

    @Test
    public void test_setValue() {
        redissonService.setValue("demo", "test123");
        log.info("设置属性值");
    }

    @Test
    public void test_getValue() {
        String value = redissonService.getValue("demo");
        log.info("测试结果:{}", value);
    }

    @Test
    public void test_remove() {
        redissonService.remove("60711088280");
    }

    /**
     * 可重入锁，加锁和解锁。Redisson的分布式可重入锁RLock Java对象实现了java.util.concurrent.locks.Lock接口，同时还支持自动过期解锁。
     * lock.lock();
     * lock.lock(10, TimeUnit.SECONDS);
     * lock.tryLock(3, 10, TimeUnit.SECONDS);
     * lock.lockAsync();
     * lock.lockAsync(10, TimeUnit.SECONDS);
     * Future<Boolean> res = lock.tryLockAsync(3, 10, TimeUnit.SECONDS);
     */
    @Test
    public void test_ReentrantLock() throws Exception {
        RLock lock = redissonService.getLock("");
        try {
            // 1. 最常见的使用方法
            lock.lock();

        } finally {
            lock.unlock();
        }
    }

}
