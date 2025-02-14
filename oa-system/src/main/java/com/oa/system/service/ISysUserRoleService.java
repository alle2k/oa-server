package com.oa.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.system.domain.SysUserRole;

import java.util.Collection;
import java.util.List;

/**
 * @auther CodeGenerator
 * @create 2025-02-13 17:30:49
 * @describe 用户和角色关联表服务类
 */
public interface ISysUserRoleService extends IService<SysUserRole> {

    /**
     * 根据角色ID获取关联的用户列表
     *
     * @param roleIds 角色Id列表
     * @return List
     */
    default List<SysUserRole> selectListByRoleIds(Collection<Long> roleIds) {
        return list(Wrappers.<SysUserRole>lambdaQuery()
                .in(SysUserRole::getRoleId, roleIds));
    }
}
