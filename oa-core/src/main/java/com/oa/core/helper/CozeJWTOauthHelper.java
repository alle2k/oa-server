package com.oa.core.helper;

import com.coze.openapi.client.auth.OAuthToken;
import com.coze.openapi.service.auth.JWTOAuthClient;
import com.oa.common.constant.CacheConstants;
import com.oa.common.core.redis.RedisCache;
import com.oa.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CozeJWTOauthHelper {

    private static final int MIN_EXPIRE_SECOND = 300;

    @Resource
    private JWTOAuthClient jwOAuthClient;
    @Resource
    private RedisCache redisCache;

    public String getAccessToken(Long userId) {
        if (Objects.isNull(userId)) {
            log.error("userId为空");
            return StringUtils.EMPTY;
        }
        String sessionName = String.valueOf(userId);
        String redisKey = CacheConstants.COZE_ACCESS_TOKEN_KEY + sessionName;
        OAuthToken token = redisCache.getCacheObject(redisKey, OAuthToken.class);
        if (!Objects.isNull(token) && !StringUtils.isBlank(token.getAccessToken())) {
            long expire = redisCache.getExpire(redisKey);
            if (expire > MIN_EXPIRE_SECOND) {
                return token.getAccessToken();
            }
        }
        token = jwOAuthClient.getAccessToken(sessionName);
        log.info("[coze获取AccessToken]响应：{}", token);
        if (Objects.isNull(token)) {
            log.error("[coze获取AccessToken失败]sessionName：{}", sessionName);
            return StringUtils.EMPTY;
        }
        if (StringUtils.isBlank(token.getAccessToken())) {
            log.error("[coze获取AccessToken为空]sessionName：{}，响应：{}", sessionName, token);
            return StringUtils.EMPTY;
        }
        redisCache.setCacheObject(redisKey, token, 1, TimeUnit.DAYS);
        return token.getAccessToken();
    }
}
