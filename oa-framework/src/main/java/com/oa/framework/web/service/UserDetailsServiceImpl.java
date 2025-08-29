package com.oa.framework.web.service;

import com.oa.common.core.domain.entity.SysUser;
import com.oa.common.core.domain.model.LoginUser;
import com.oa.common.enums.UserStatus;
import com.oa.common.exception.ServiceException;
import com.oa.common.utils.MessageUtils;
import com.oa.common.utils.StringUtils;
import com.oa.system.helper.DataPermissionHelper;
import com.oa.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户验证处理
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Resource
    private ISysUserService userService;
    @Resource
    private SysPasswordService passwordService;
    @Resource
    private SysPermissionService permissionService;
    @Resource
    private DataPermissionHelper dataPermissionHelper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user)) {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException(MessageUtils.message("user.not.exists"));
        } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException(MessageUtils.message("user.password.delete"));
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException(MessageUtils.message("user.blocked"));
        }

        passwordService.validate(user);

        return createLoginUser(user);
    }

    public UserDetails createLoginUser(SysUser user) {
        return dataPermissionHelper.populate(new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user)));
    }
}
