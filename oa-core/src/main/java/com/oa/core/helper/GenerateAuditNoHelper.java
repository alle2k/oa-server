package com.oa.core.helper;

import com.oa.common.core.redis.RedisCache;
import com.oa.common.utils.DateUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class GenerateAuditNoHelper implements InitializingBean {

    @Resource
    private RedisCache redisCache;

    public String get() {
        String date = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date());
        Integer value = redisCache.getCacheObject(date);
        if (Objects.isNull(value)) {
            Long increment = redisCache.incr(date);
            redisCache.expire(date, 1, TimeUnit.DAYS);
            return date + String.format("%04d", increment);
        }
        return date + String.format("%04d", redisCache.incr(date));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String date = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date());
        Integer value = redisCache.getCacheObject(date);
        if (!Objects.isNull(value)) {
            return;
        }
        redisCache.incr(date);
        redisCache.expire(date, 1, TimeUnit.DAYS);
    }
}
