package com.oa.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.system.domain.SysRoleMenu;

import java.util.Collection;
import java.util.List;

/**
 * @auther CodeGenerator
 * @create 2025-04-25 11:07:42
 * @describe 角色和菜单关联表服务类
 */
public interface ISysRoleMenuService extends IService<SysRoleMenu> {

    default List<SysRoleMenu> selectListByRoleIds(Collection<Long> roleIds) {
        return list(Wrappers.<SysRoleMenu>lambdaQuery()
                .in(SysRoleMenu::getRoleId, roleIds));
    }
}
