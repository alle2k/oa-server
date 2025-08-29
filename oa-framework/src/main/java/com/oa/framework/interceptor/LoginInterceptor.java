package com.oa.framework.interceptor;

import com.oa.common.core.domain.model.LoginUser;
import com.oa.common.error.BaseCode;
import com.oa.common.exception.ServiceException;
import com.oa.common.utils.spring.SpringUtils;
import com.oa.framework.config.properties.PermitAllUrlProperties;
import com.oa.framework.web.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 登录拦截器
 *
 * @author jiangdawei on 2018/8/22 18:29.
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        if (SpringUtils.getBean(PermitAllUrlProperties.class).getUrls().stream()
                .anyMatch(url -> request.getRequestURI().equals(url))) {
            return true;
        }
        String token = tokenService.getToken(request);
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (Objects.isNull(loginUser)) {
            log.info("token过期，token：{}", token);
            throw new ServiceException(BaseCode.LOGIN_INVALID);
        }
        if (!loginUser.getToken().equals(token)) {
            log.info("token与缓存中不一致，token：{}，userId：{}", token, loginUser.getUserId());
            throw new ServiceException(BaseCode.LOGIN_INVALID);
        }
        return true;
    }
}
