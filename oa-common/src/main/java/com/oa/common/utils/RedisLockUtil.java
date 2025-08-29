package com.oa.common.utils;


import cn.hutool.core.lang.UUID;
import com.oa.common.core.redis.RedisCache;
import com.oa.common.error.BaseCode;
import com.oa.common.exception.ServiceException;
import com.oa.common.utils.spring.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public class RedisLockUtil {

    public static void lock(String key, long timeout, TimeUnit timeUnit) {
        lock(key, timeout, timeUnit, "当前任务正在进行中");
    }

    public static void lock(String key, long timeout, TimeUnit timeUnit, String errMsg) {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        boolean setSuccess = redisCache.setnx(key, "1", timeout, timeUnit);
        if (!setSuccess) {
            throw new ServiceException(BaseCode.PARAM_ERROR.getCode(), errMsg);
        }
    }

    public static void unlock(String key) {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        redisCache.deleteObject(Collections.singletonList(key));
    }

    public static String acquire(String key, long timeout, TimeUnit timeUnit, long acquireTimeout) {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        String identifier = UUID.fastUUID().toString();
        long end = System.currentTimeMillis() + acquireTimeout;
        int attempts = 0;
        while (System.currentTimeMillis() < end) {
            if (redisCache.setnx(key, identifier, timeout, timeUnit)) {
                return identifier;
            }
            try {
                Thread.sleep(calculateExponentialBackoff(attempts++));
            } catch (InterruptedException e) {
                log.error("获取锁失败，key：{}", key, e);
                throw new ServiceException(BaseCode.BIZ_ERROR);
            }
        }
        throw new ServiceException(BaseCode.BIZ_ERROR.getCode(), "当前正忙，请稍后再试");
    }

    private static long calculateExponentialBackoff(int attempts) {
        // 指数退避计算，最大等待时间设置为 1 秒
        return Math.min(100 * (1L << attempts), 1000);
    }

    public static void release(String key, String identifier) {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        String value = redisCache.getCacheObject(key);
        if (value != null && value.equals(identifier)) { // 判断是否是当前持有锁的客户端
            redisCache.deleteObject(Collections.singletonList(key));
        }
    }

    /**
     * 根据业务键获取锁
     *
     * @param lockKey  键
     * @param timeout  过期时间
     * @param timeUnit 过期时间单位
     * @param supplier 供应方法
     * @param <T>      返回值类型
     * @return 供应方法的返回值
     */
    public static <T> T acquire(String lockKey, long timeout, TimeUnit timeUnit, Supplier<T> supplier) {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        boolean flag = redisCache.setnx(lockKey, StringUtils.EMPTY, timeout, timeUnit);
        if (!flag) {
            throw new ServiceException(BaseCode.BIZ_ERROR.getCode(), "当前正忙，请稍后再试");
        }
        try {
            return supplier.get();
        } finally {
            redisCache.deleteObject(lockKey);
        }
    }

    /**
     * 加锁
     *
     * @param key      锁
     * @param timeout  超时时间
     * @param timeUnit 超时时间单位
     * @return 加锁令牌
     */
    public static String acquire(String key, long timeout, TimeUnit timeUnit) {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        String identifier = UUID.fastUUID().toString();
        if (redisCache.setnx(key, identifier, timeout, timeUnit)) {
            return identifier;
        }
        throw new ServiceException(BaseCode.BIZ_ERROR.getCode(), "当前任务正在进行中");
    }
}
