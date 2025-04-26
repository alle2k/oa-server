package com.oa.web.controller.system;

import com.oa.common.annotation.Log;
import com.oa.common.core.controller.BaseController;
import com.oa.common.core.domain.AjaxResult;
import com.oa.common.core.domain.entity.SysDept;
import com.oa.common.core.domain.entity.SysMenu;
import com.oa.common.core.domain.entity.SysRole;
import com.oa.common.core.domain.entity.SysUser;
import com.oa.common.core.domain.model.LoginUser;
import com.oa.common.core.page.TableDataInfo;
import com.oa.common.enums.BusinessType;
import com.oa.common.utils.poi.ExcelUtil;
import com.oa.framework.web.service.TokenService;
import com.oa.system.domain.SysRoleMenu;
import com.oa.system.domain.SysUserRole;
import com.oa.system.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 角色信息
 */
@RestController
@RequestMapping("/system/role")
public class SysRoleController extends BaseController {

    @Resource
    private ISysRoleService roleService;
    @Resource
    private TokenService tokenService;
    @Resource
    private ISysUserService userService;
    @Resource
    private ISysDeptService deptService;
    @Resource
    private ISysUserRoleService sysUserRoleService;
    @Resource
    private ISysMenuService sysMenuService;
    @Resource
    private ISysRoleMenuService sysRoleMenuService;

    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysRole role) {
        startPage();
        List<SysRole> list = roleService.selectRoleList(role);
        return getDataTable(list);
    }

    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:role:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysRole role) {
        List<SysRole> list = roleService.selectRoleList(role);
        ExcelUtil<SysRole> util = new ExcelUtil<SysRole>(SysRole.class);
        util.exportExcel(response, list, "角色数据");
    }

    /**
     * 根据角色编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping(value = "/{roleId}")
    public AjaxResult getInfo(@PathVariable Long roleId) {
        roleService.checkRoleDataScope(roleId);
        return success(roleService.selectRoleById(roleId));
    }

    /**
     * 新增角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:add')")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysRole role) {
        if (!roleService.checkRoleNameUnique(role)) {
            return error("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(role)) {
            return error("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setCreateBy(getUsername());
        return toAjax(roleService.insertRole(role));

    }

    /**
     * 修改保存角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        if (!roleService.checkRoleNameUnique(role)) {
            return error("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(role)) {
            return error("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setUpdateBy(getUsername());

        if (roleService.updateRole(role) > 0) {
            List<SysUserRole> userRoleList = sysUserRoleService.selectListByRoleIds(Collections.singleton(role.getRoleId()));
            if (CollectionUtils.isEmpty(userRoleList)) {
                return success();
            }
            List<SysUser> userList = userService.selectListByUserIds(userRoleList.stream().map(SysUserRole::getUserId).collect(Collectors.toSet()));
            if (CollectionUtils.isEmpty(userList)) {
                return success();
            }
            Map<Long, SysUser> userMap = userList.stream().collect(Collectors.toMap(SysUser::getUserId, Function.identity()));
            Map<Long, Set<Long>> userRoleMap = sysUserRoleService.selectListByUserIds(userMap.keySet()).stream().collect(Collectors.groupingBy(SysUserRole::getUserId, Collectors.mapping(SysUserRole::getRoleId, Collectors.toSet())));
            if (CollectionUtils.isEmpty(userRoleMap)) {
                return success();
            }
            Map<Long, Set<Long>> roleMenuMap = sysRoleMenuService.selectListByRoleIds(userRoleMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()))
                    .stream().collect(Collectors.groupingBy(SysRoleMenu::getRoleId, Collectors.mapping(SysRoleMenu::getMenuId, Collectors.toSet())));
            if (CollectionUtils.isEmpty(roleMenuMap)) {
                return success();
            }
            Map<Long, SysMenu> menuMap = sysMenuService.listByIds(roleMenuMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()))
                    .stream().collect(Collectors.toMap(SysMenu::getMenuId, Function.identity()));
            if (CollectionUtils.isEmpty(menuMap)) {
                return success();
            }
            // 更新缓存用户权限
            userList.forEach(x -> {
                Set<String> permissions = new HashSet<>();
                LoginUser loginUser = tokenService.getLoginUserById(x.getUserId());
                if (Objects.isNull(loginUser)) {
                    return;
                }
                loginUser.setUser(userMap.get(x.getUserId()));

                Set<Long> roleIds = userRoleMap.get(x.getUserId());
                if (CollectionUtils.isEmpty(roleIds)) {
                    return;
                }
                Iterator<Long> iterator = roleIds.iterator();
                do {
                    Long roleId = iterator.next();
                    Set<Long> menuIds = roleMenuMap.get(roleId);
                    if (CollectionUtils.isEmpty(menuIds)) {
                        continue;
                    }
                    menuIds.forEach(y -> {
                        SysMenu menu = menuMap.get(y);
                        if (Objects.isNull(menu)) {
                            return;
                        }
                        permissions.add(menu.getPerms());
                    });
                } while (iterator.hasNext());
                loginUser.setPermissions(permissions);
                tokenService.setLoginUser(loginUser);
            });
            return success();
        }
        return error("修改角色'" + role.getRoleName() + "'失败，请联系管理员");
    }

    /**
     * 修改保存数据权限
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/dataScope")
    public AjaxResult dataScope(@RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        return toAjax(roleService.authDataScope(role));
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        role.setUpdateBy(getUsername());
        return toAjax(roleService.updateRoleStatus(role));
    }

    /**
     * 删除角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:remove')")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public AjaxResult remove(@PathVariable Long[] roleIds) {
        return toAjax(roleService.deleteRoleByIds(roleIds));
    }

    /**
     * 获取角色选择框列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping("/optionselect")
    public AjaxResult optionselect() {
        return success(roleService.selectRoleAll());
    }

    /**
     * 查询已分配用户角色列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/allocatedList")
    public TableDataInfo allocatedList(SysUser user) {
        startPage();
        List<SysUser> list = userService.selectAllocatedList(user);
        return getDataTable(list);
    }

    /**
     * 查询未分配用户角色列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/unallocatedList")
    public TableDataInfo unallocatedList(SysUser user) {
        startPage();
        List<SysUser> list = userService.selectUnallocatedList(user);
        return getDataTable(list);
    }

    /**
     * 取消授权用户
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancel")
    public AjaxResult cancelAuthUser(@RequestBody SysUserRole userRole) {
        return toAjax(roleService.deleteAuthUser(userRole));
    }

    /**
     * 批量取消授权用户
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancelAll")
    public AjaxResult cancelAuthUserAll(Long roleId, Long[] userIds) {
        return toAjax(roleService.deleteAuthUsers(roleId, userIds));
    }

    /**
     * 批量选择用户授权
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/selectAll")
    public AjaxResult selectAuthUserAll(Long roleId, Long[] userIds) {
        roleService.checkRoleDataScope(roleId);
        return toAjax(roleService.insertAuthUsers(roleId, userIds));
    }

    /**
     * 获取对应角色部门树列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping(value = "/deptTree/{roleId}")
    public AjaxResult deptTree(@PathVariable("roleId") Long roleId) {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", deptService.selectDeptListByRoleId(roleId));
        ajax.put("depts", deptService.selectDeptTreeList(new SysDept()));
        return ajax;
    }
}
