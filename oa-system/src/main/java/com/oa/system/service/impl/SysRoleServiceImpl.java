package com.oa.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.constant.CacheConstants;
import com.oa.common.constant.TransactionConstant;
import com.oa.common.constant.UserConstants;
import com.oa.common.core.domain.entity.SysRole;
import com.oa.common.core.domain.entity.SysUser;
import com.oa.common.core.domain.model.DataPermissionDto;
import com.oa.common.core.domain.model.LoginUser;
import com.oa.common.core.redis.RedisCache;
import com.oa.common.exception.ServiceException;
import com.oa.common.utils.SecurityUtils;
import com.oa.common.utils.StringUtils;
import com.oa.common.utils.spring.SpringUtils;
import com.oa.system.domain.SysRoleDept;
import com.oa.system.domain.SysRoleMenu;
import com.oa.system.domain.SysUserRole;
import com.oa.system.helper.DataPermissionHelper;
import com.oa.system.mapper.master.SysRoleDeptMapper;
import com.oa.system.mapper.master.SysRoleMapper;
import com.oa.system.mapper.master.SysRoleMenuMapper;
import com.oa.system.mapper.master.SysUserRoleMapper;
import com.oa.system.service.ISysDeptService;
import com.oa.system.service.ISysRoleService;
import com.oa.system.service.ISysUserRoleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 角色 业务层处理
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Resource
    private SysRoleMapper roleMapper;
    @Resource
    private SysRoleMenuMapper roleMenuMapper;
    @Resource
    private SysUserRoleMapper userRoleMapper;
    @Resource
    private SysRoleDeptMapper roleDeptMapper;
    @Resource
    private ISysUserRoleService sysUserRoleService;
    @Resource
    private ISysRoleService sysRoleService;
    @Resource
    private ISysDeptService sysDeptService;
    @Resource
    private RedisCache redisCache;

    @Value("${token.expireTime}")
    private int expireTime;

    /**
     * 根据条件分页查询角色数据
     *
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    @Override
    public List<SysRole> selectRoleList(SysRole role) {
        return roleMapper.selectRoleList(role);
    }

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        List<SysRole> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        List<SysRole> roles = selectRoleAll();
        for (SysRole role : roles) {
            for (SysRole userRole : userRoles) {
                if (role.getRoleId().longValue() == userRole.getRoleId().longValue()) {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId) {
        List<SysRole> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms) {
            if (StringUtils.isNotNull(perm)) {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    @Override
    public List<SysRole> selectRoleAll() {
        return SpringUtils.getAopProxy(this).selectRoleList(new SysRole());
    }

    /**
     * 根据用户ID获取角色选择框列表
     *
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    @Override
    public List<Long> selectRoleListByUserId(Long userId) {
        return roleMapper.selectRoleListByUserId(userId);
    }

    /**
     * 通过角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    @Override
    public SysRole selectRoleById(Long roleId) {
        return roleMapper.selectRoleById(roleId);
    }

    /**
     * 校验角色名称是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleNameUnique(SysRole role) {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleNameUnique(role.getRoleName());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色权限是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleKeyUnique(SysRole role) {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleKeyUnique(role.getRoleKey());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色是否允许操作
     *
     * @param role 角色信息
     */
    @Override
    public void checkRoleAllowed(SysRole role) {
        if (StringUtils.isNotNull(role.getRoleId()) && role.isAdmin()) {
            throw new ServiceException("不允许操作超级管理员角色");
        }
    }

    /**
     * 校验角色是否有数据权限
     *
     * @param roleIds 角色id
     */
    @Override
    public void checkRoleDataScope(Long... roleIds) {
        if (!SysUser.isAdmin(SecurityUtils.getUserId())) {
            for (Long roleId : roleIds) {
                SysRole role = new SysRole();
                role.setRoleId(roleId);
                List<SysRole> roles = SpringUtils.getAopProxy(this).selectRoleList(role);
                if (StringUtils.isEmpty(roles)) {
                    throw new ServiceException("没有权限访问角色数据！");
                }
            }
        }
    }

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public int countUserRoleByRoleId(Long roleId) {
        return userRoleMapper.countUserRoleByRoleId(roleId);
    }

    /**
     * 新增保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(TransactionConstant.MASTER)
    public int insertRole(SysRole role) {
        // 新增角色信息
        roleMapper.insertRole(role);
        return insertRoleMenu(role);
    }

    /**
     * 修改保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(TransactionConstant.MASTER)
    public int updateRole(SysRole role) {
        // 修改角色信息
        roleMapper.updateRole(role);
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(role.getRoleId());
        return insertRoleMenu(role);
    }

    /**
     * 修改角色状态
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public int updateRoleStatus(SysRole role) {
        return roleMapper.updateRole(role);
    }

    /**
     * 修改数据权限信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(TransactionConstant.MASTER)
    public int authDataScope(SysRole role) {
        // 修改角色信息
        roleMapper.updateRole(role);
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDeptByRoleId(role.getRoleId());
        // 新增角色和部门信息（数据权限）
        insertRoleDept(role);

        List<SysUserRole> userRoleList = sysUserRoleService.selectListByRoleIds(Collections.singleton(role.getRoleId()));
        if (CollectionUtils.isEmpty(userRoleList)) {
            return 1;
        }
        Set<Long> userIds = userRoleList.stream().map(SysUserRole::getUserId).collect(Collectors.toSet());
        Map<Long, Set<Long>> userRoleMap = sysUserRoleService.selectListByUserIds(userIds).stream().collect(Collectors.groupingBy(SysUserRole::getUserId, Collectors.mapping(SysUserRole::getRoleId, Collectors.toSet())));
        if (CollectionUtils.isEmpty(userRoleMap)) {
            return 1;
        }
        Map<Long, String> roleDataScopeMap = sysRoleService.listByIds(userRoleMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(SysRole::getRoleId, SysRole::getDataScope));
        if (CollectionUtils.isEmpty(roleDataScopeMap)) {
            return 1;
        }
        userIds.forEach(x -> {
            LoginUser loginUser = redisCache.getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + x, LoginUser.class);
            if (Objects.isNull(loginUser)) {
                return;
            }
            Set<Long> roleIds = userRoleMap.get(x);
            if (CollectionUtils.isEmpty(roleIds)) {
                return;
            }
            loginUser.setDataPermissionDto(null);
            Iterator<Long> iterator = roleIds.iterator();
            do {
                Long roleId = iterator.next();
                String dataScope = roleDataScopeMap.get(roleId);
                if (StringUtils.isBlank(dataScope)) {
                    continue;
                }
                DataPermissionDto dataPermissionDto = loginUser.getDataPermissionDto();
                boolean dataPermissionDtoNullFlag = Objects.isNull(dataPermissionDto);
                switch (dataScope) {
                    case DataPermissionHelper.DATA_SCOPE_ALL:
                        loginUser.setDataPermissionDto(new DataPermissionDto(dataScope));
                        break;
                    case DataPermissionHelper.DATA_SCOPE_DEPT_AND_CHILD:
                        loginUser.setDataPermissionDto(new DataPermissionDto(dataScope, new LinkedList<>(sysDeptService.recursiveDownGetDeptIds(loginUser.getDeptId()))));
                        break;
                    case DataPermissionHelper.DATA_SCOPE_DEPT:
                        if (!dataPermissionDtoNullFlag && DataPermissionHelper.DATA_SCOPE_DEPT_AND_CHILD.equals(dataPermissionDto.getDataScope())) {
                            break;
                        }
                        loginUser.setDataPermissionDto(new DataPermissionDto(dataScope, Collections.singletonList(sysDeptService.selectOneByDeptId(loginUser.getDeptId()).getDeptId())));
                        break;
                    case DataPermissionHelper.DATA_SCOPE_SELF:
                        if (!dataPermissionDtoNullFlag && (DataPermissionHelper.DATA_SCOPE_DEPT_AND_CHILD.equals(dataPermissionDto.getDataScope())
                                || DataPermissionHelper.DATA_SCOPE_DEPT.equals(dataPermissionDto.getDataScope()))) {
                            break;
                        }
                        loginUser.setDataPermissionDto(new DataPermissionDto(dataScope, loginUser.getUserId()));
                        break;
                    case DataPermissionHelper.DATA_SCOPE_CUSTOM:
                    default:
                        break;
                }
                if (DataPermissionHelper.DATA_SCOPE_ALL.equals(dataScope)) {
                    break;
                }
            } while (iterator.hasNext());
            redisCache.setCacheObject(CacheConstants.LOGIN_TOKEN_KEY + x, loginUser, expireTime, TimeUnit.MINUTES);
        });
        return 1;
    }

    /**
     * 新增角色菜单信息
     *
     * @param role 角色对象
     */
    public int insertRoleMenu(SysRole role) {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenu> list = new ArrayList<SysRoleMenu>();
        for (Long menuId : role.getMenuIds()) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0) {
            rows = roleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }

    /**
     * 新增角色部门信息(数据权限)
     *
     * @param role 角色对象
     */
    public int insertRoleDept(SysRole role) {
        int rows = 1;
        // 新增角色与部门（数据权限）管理
        List<SysRoleDept> list = new ArrayList<SysRoleDept>();
        for (Long deptId : role.getDeptIds()) {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(role.getRoleId());
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (list.size() > 0) {
            rows = roleDeptMapper.batchRoleDept(list);
        }
        return rows;
    }

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    @Transactional(TransactionConstant.MASTER)
    public int deleteRoleById(Long roleId) {
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(roleId);
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDeptByRoleId(roleId);
        return roleMapper.deleteRoleById(roleId);
    }

    /**
     * 批量删除角色信息
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    @Override
    @Transactional(TransactionConstant.MASTER)
    public int deleteRoleByIds(Long[] roleIds) {
        for (Long roleId : roleIds) {
            checkRoleAllowed(new SysRole(roleId));
            checkRoleDataScope(roleId);
            SysRole role = selectRoleById(roleId);
            if (countUserRoleByRoleId(roleId) > 0) {
                throw new ServiceException(String.format("%1$s已分配,不能删除", role.getRoleName()));
            }
        }
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenu(roleIds);
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDept(roleIds);
        return roleMapper.deleteRoleByIds(roleIds);
    }

    /**
     * 取消授权用户角色
     *
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    @Override
    public int deleteAuthUser(SysUserRole userRole) {
        return userRoleMapper.deleteUserRoleInfo(userRole);
    }

    /**
     * 批量取消授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要取消授权的用户数据ID
     * @return 结果
     */
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds) {
        return userRoleMapper.deleteUserRoleInfos(roleId, userIds);
    }

    /**
     * 批量选择授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要授权的用户数据ID
     * @return 结果
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds) {
        // 新增用户与角色管理
        List<SysUserRole> list = new ArrayList<SysUserRole>();
        for (Long userId : userIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return userRoleMapper.batchUserRole(list);
    }
}
