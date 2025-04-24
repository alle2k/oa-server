package com.oa.framework.web.service;

import com.oa.common.core.domain.entity.SysRole;
import com.oa.common.core.domain.entity.SysUser;
import com.oa.common.core.domain.model.DataPermissionDto;
import com.oa.common.core.domain.model.LoginUser;
import com.oa.common.enums.UserStatus;
import com.oa.common.exception.ServiceException;
import com.oa.common.utils.MessageUtils;
import com.oa.common.utils.SecurityUtils;
import com.oa.common.utils.StringUtils;
import com.oa.framework.aspectj.DataScopeAspect;
import com.oa.system.service.ISysDeptService;
import com.oa.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

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
    private ISysDeptService sysDeptService;

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
        LoginUser loginUser = new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
        Set<String> dataScopes = loginUser.getUser().getRoles().stream().map(SysRole::getDataScope).collect(Collectors.toSet());
        if (dataScopes.contains(DataScopeAspect.DATA_SCOPE_ALL)) {
            loginUser.setDataPermissionDto(new DataPermissionDto(DataScopeAspect.DATA_SCOPE_ALL));
        }
        if (dataScopes.contains(DataScopeAspect.DATA_SCOPE_DEPT_AND_CHILD)) {
            loginUser.setDataPermissionDto(new DataPermissionDto(DataScopeAspect.DATA_SCOPE_DEPT_AND_CHILD, new LinkedList<>(sysDeptService.recursiveDownGetDeptIds(user.getDeptId()))));
        }
        if (dataScopes.contains(DataScopeAspect.DATA_SCOPE_DEPT)) {
            loginUser.setDataPermissionDto(new DataPermissionDto(DataScopeAspect.DATA_SCOPE_DEPT, Collections.singletonList(sysDeptService.selectOneByDeptId(user.getDeptId()).getDeptId())));
        }
        if (dataScopes.contains(DataScopeAspect.DATA_SCOPE_SELF)) {
            loginUser.setDataPermissionDto(new DataPermissionDto(DataScopeAspect.DATA_SCOPE_SELF, user.getUserId()));
        }
        return loginUser;
    }
}
