package com.oa.system.helper;

import com.oa.common.core.domain.entity.SysRole;
import com.oa.common.core.domain.model.DataPermissionDto;
import com.oa.common.core.domain.model.LoginUser;
import com.oa.system.service.ISysDeptService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataPermissionHelper {

    /**
     * 全部数据权限
     */
    public static final String DATA_SCOPE_ALL = "1";

    /**
     * 自定数据权限
     */
    public static final String DATA_SCOPE_CUSTOM = "2";

    /**
     * 部门数据权限
     */
    public static final String DATA_SCOPE_DEPT = "3";

    /**
     * 部门及以下数据权限
     */
    public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";

    /**
     * 仅本人数据权限
     */
    public static final String DATA_SCOPE_SELF = "5";

    /**
     * 数据权限过滤关键字
     */
    public static final String DATA_SCOPE = "dataScope";

    @Resource
    private ISysDeptService sysDeptService;

    public LoginUser populate(LoginUser loginUser) {
        Set<String> dataScopes = loginUser.getUser().getRoles().stream().map(SysRole::getDataScope).collect(Collectors.toSet());
        if (dataScopes.contains(DATA_SCOPE_ALL)) {
            loginUser.setDataPermissionDto(new DataPermissionDto(DATA_SCOPE_ALL));
        } else if (dataScopes.contains(DATA_SCOPE_DEPT_AND_CHILD)) {
            loginUser.setDataPermissionDto(new DataPermissionDto(DATA_SCOPE_DEPT_AND_CHILD, new LinkedList<>(sysDeptService.recursiveDownGetDeptIds(loginUser.getDeptId()))));
        } else if (dataScopes.contains(DATA_SCOPE_DEPT)) {
            loginUser.setDataPermissionDto(new DataPermissionDto(DATA_SCOPE_DEPT, Collections.singletonList(sysDeptService.selectOneByDeptId(loginUser.getDeptId()).getDeptId())));
        } else if (dataScopes.contains(DATA_SCOPE_SELF)) {
            loginUser.setDataPermissionDto(new DataPermissionDto(DATA_SCOPE_SELF, loginUser.getUserId()));
        }
        return loginUser;
    }
}
