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
        Long value = redisCache.<Long>getCacheObject(date);
        if (Objects.isNull(value)) {
            redisCache.setCacheObject(date, 1L, 1, TimeUnit.DAYS);
            return date + "0001";
        }
        return date + String.format("%04d", redisCache.incr(date));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String date = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date());
        Long value = redisCache.<Long>getCacheObject(date);
        if (!Objects.isNull(value)) {
            return;
        }
        redisCache.setCacheObject(date, 1L, 1, TimeUnit.DAYS);
    }
}
