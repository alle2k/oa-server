package com.oa.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.constant.UserConstants;
import com.oa.common.core.domain.TreeSelect;
import com.oa.common.core.domain.entity.SysDept;
import com.oa.common.core.domain.entity.SysRole;
import com.oa.common.core.domain.entity.SysUser;
import com.oa.common.core.text.Convert;
import com.oa.common.exception.ServiceException;
import com.oa.common.utils.SecurityUtils;
import com.oa.common.utils.StringUtils;
import com.oa.common.utils.spring.SpringUtils;
import com.oa.system.mapper.master.SysDeptMapper;
import com.oa.system.mapper.master.SysRoleMapper;
import com.oa.system.service.ISysDeptService;
import com.oa.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门管理 服务实现
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {
    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Resource
    private ISysUserService userService;

    /**
     * 查询部门管理数据
     *
     * @param dept 部门信息
     * @return 部门信息集合
     */
    @Override
    public List<SysDept> selectDeptList(SysDept dept) {
        return deptMapper.selectDeptList(dept);
    }

    /**
     * 查询部门树结构信息
     *
     * @param dept 部门信息
     * @return 部门树信息集合
     */
    @Override
    public List<TreeSelect> selectDeptTreeList(SysDept dept) {
        List<SysDept> depts = SpringUtils.getAopProxy(this).selectDeptList(dept);
        return buildDeptTreeSelect(depts);
    }

    /**
     * 构建前端所需要树结构
     *
     * @param depts 部门列表
     * @return 树结构列表
     */
    @Override
    public List<SysDept> buildDeptTree(List<SysDept> depts) {
        List<SysDept> returnList = new ArrayList<SysDept>();
        List<Long> tempList = depts.stream().map(SysDept::getDeptId).collect(Collectors.toList());
        for (SysDept dept : depts) {
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(dept.getParentId())) {
                recursionFn(depts, dept);
                returnList.add(dept);
            }
        }
        if (returnList.isEmpty()) {
            returnList = depts;
        }
        return returnList;
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param depts 部门列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelect> buildDeptTreeSelect(List<SysDept> depts) {
        List<SysDept> deptTrees = buildDeptTree(depts);
        return deptTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    @Override
    public List<Long> selectDeptListByRoleId(Long roleId) {
        SysRole role = roleMapper.selectRoleById(roleId);
        return deptMapper.selectDeptListByRoleId(roleId, role.isDeptCheckStrictly());
    }

    /**
     * 根据部门ID查询信息
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    @Override
    public SysDept selectDeptById(Long deptId) {
        return deptMapper.selectDeptById(deptId);
    }

    /**
     * 根据ID查询所有子部门（正常状态）
     *
     * @param deptId 部门ID
     * @return 子部门数
     */
    @Override
    public int selectNormalChildrenDeptById(Long deptId) {
        return deptMapper.selectNormalChildrenDeptById(deptId);
    }

    /**
     * 是否存在子节点
     *
     * @param deptId 部门ID
     * @return 结果
     */
    @Override
    public boolean hasChildByDeptId(Long deptId) {
        int result = deptMapper.hasChildByDeptId(deptId);
        return result > 0;
    }

    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    @Override
    public boolean checkDeptExistUser(Long deptId) {
        int result = deptMapper.checkDeptExistUser(deptId);
        return result > 0;
    }

    /**
     * 校验部门名称是否唯一
     *
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public boolean checkDeptNameUnique(SysDept dept) {
        Long deptId = StringUtils.isNull(dept.getDeptId()) ? -1L : dept.getDeptId();
        SysDept info = deptMapper.checkDeptNameUnique(dept.getDeptName(), dept.getParentId());
        if (StringUtils.isNotNull(info) && info.getDeptId().longValue() != deptId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验部门是否有数据权限
     *
     * @param deptId 部门id
     */
    @Override
    public void checkDeptDataScope(Long deptId) {
        if (!SysUser.isAdmin(SecurityUtils.getUserId()) && StringUtils.isNotNull(deptId)) {
            SysDept dept = new SysDept();
            dept.setDeptId(deptId);
            List<SysDept> depts = SpringUtils.getAopProxy(this).selectDeptList(dept);
            if (StringUtils.isEmpty(depts)) {
                throw new ServiceException("没有权限访问部门数据！");
            }
        }
    }

    /**
     * 新增保存部门信息
     *
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public int insertDept(SysDept dept) {
        SysDept info = deptMapper.selectDeptById(dept.getParentId());
        // 如果父节点不为正常状态,则不允许新增子节点
        if (!UserConstants.DEPT_NORMAL.equals(info.getStatus())) {
            throw new ServiceException("部门停用，不允许新增");
        }
        dept.setAncestors(info.getAncestors() + "," + dept.getParentId());
        return deptMapper.insertDept(dept);
    }

    /**
     * 修改保存部门信息
     *
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public int updateDept(SysDept dept) {
        SysDept newParentDept = deptMapper.selectDeptById(dept.getParentId());
        SysDept oldDept = deptMapper.selectDeptById(dept.getDeptId());
        if (StringUtils.isNotNull(newParentDept) && StringUtils.isNotNull(oldDept)) {
            String newAncestors = newParentDept.getAncestors() + "," + newParentDept.getDeptId();
            String oldAncestors = oldDept.getAncestors();
            dept.setAncestors(newAncestors);
            updateDeptChildren(dept.getDeptId(), newAncestors, oldAncestors);
        }
        int result = deptMapper.updateDept(dept);
        if (UserConstants.DEPT_NORMAL.equals(dept.getStatus()) && StringUtils.isNotEmpty(dept.getAncestors())
                && !StringUtils.equals("0", dept.getAncestors())) {
            // 如果该部门是启用状态，则启用该部门的所有上级部门
            updateParentDeptStatusNormal(dept);
        }
        return result;
    }

    /**
     * 修改该部门的父级部门状态
     *
     * @param dept 当前部门
     */
    private void updateParentDeptStatusNormal(SysDept dept) {
        String ancestors = dept.getAncestors();
        Long[] deptIds = Convert.toLongArray(ancestors);
        deptMapper.updateDeptStatusNormal(deptIds);
    }

    /**
     * 修改子元素关系
     *
     * @param deptId       被修改的部门ID
     * @param newAncestors 新的父ID集合
     * @param oldAncestors 旧的父ID集合
     */
    public void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors) {
        List<SysDept> children = deptMapper.selectChildrenDeptById(deptId);
        for (SysDept child : children) {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }
        if (children.size() > 0) {
            deptMapper.updateDeptChildren(children);
        }
    }

    /**
     * 删除部门管理信息
     *
     * @param deptId 部门ID
     * @return 结果
     */
    @Override
    public int deleteDeptById(Long deptId) {
        return deptMapper.deleteDeptById(deptId);
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<SysDept> list, SysDept t) {
        // 得到子节点列表
        List<SysDept> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysDept tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysDept> getChildList(List<SysDept> list, SysDept t) {
        List<SysDept> tlist = new ArrayList<SysDept>();
        Iterator<SysDept> it = list.iterator();
        while (it.hasNext()) {
            SysDept n = (SysDept) it.next();
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getDeptId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysDept> list, SysDept t) {
        return getChildList(list, t).size() > 0;
    }

    @Override
    public Set<Long> recursiveGetDeptLeader(Collection<Long> deptIds) {
        List<SysDept> deptList = listByIds(deptIds);
        if (CollectionUtils.isEmpty(deptList)) {
            return Collections.emptySet();
        }
        Set<Long> leaderIds = deptList.stream().map(SysDept::getLeader).collect(Collectors.toSet());
        doRecursiveGetDeptLeader(deptList.stream().map(SysDept::getParentId)
                .filter(x -> x > 0).collect(Collectors.toList()), leaderIds);
        return leaderIds;
    }

    /**
     * 递归获取上级部门领导
     *
     * @param deptIds   部门ID列表
     * @param leaderIds 部门领导ID列表
     */
    private void doRecursiveGetDeptLeader(List<Long> deptIds, Set<Long> leaderIds) {
        if (CollectionUtils.isEmpty(deptIds)) {
            return;
        }
        List<SysDept> deptList = listByIds(deptIds);
        leaderIds.addAll(deptList.stream().map(SysDept::getLeader).collect(Collectors.toList()));
        doRecursiveGetDeptLeader(deptList.stream().map(SysDept::getParentId)
                .filter(x -> x > 0).collect(Collectors.toList()), leaderIds);
    }

    @Override
    public Set<Long> fetchDeptUserIds(Long deptId) {
        Set<Long> deptIds = recursiveDownGetDeptIds(deptId);
        if (CollectionUtils.isEmpty(deptIds)) {
            return Collections.emptySet();
        }
        List<SysUser> userList = userService.selectListByDeptIds(deptIds);
        if (CollectionUtils.isEmpty(userList)) {
            return Collections.emptySet();
        }
        return userList.stream().map(SysUser::getUserId).collect(Collectors.toSet());
    }

    @Override
    public Set<Long> recursiveDownGetDeptIds(Long deptId) {
        if (Objects.isNull(deptId)) {
            deptId = 0L;
        }
        Map<Long, List<SysDept>> deptMap = list().stream().filter(x -> !x.getDelFlag().equals("2")).collect(Collectors.toList())
                .stream().collect(Collectors.groupingBy(SysDept::getParentId));
        if (CollectionUtils.isEmpty(deptMap)) {
            return Collections.emptySet();
        }
        Set<Long> matchDeptIds = new HashSet<>();
        matchDeptIds.add(deptId);
        recursiveDownGetDeptIds(deptMap, deptMap.get(deptId), matchDeptIds);
        return matchDeptIds;
    }

    /**
     * Recursively traverses a department hierarchy and collects the IDs of all departments.
     * The traversal starts from the given list of departments, and for each department,
     * it adds the department's ID to the result collection and then continues to traverse
     * its child departments.
     *
     * @param deptMap   A map where the key is the department ID and the value is a list of child departments.
     * @param deptList  The list of departments to start the recursive traversal from.
     * @param resultIds The collection that will hold the IDs of all traversed departments.
     */
    private void recursiveDownGetDeptIds(Map<Long, List<SysDept>> deptMap, Collection<SysDept> deptList, Collection<Long> resultIds) {
        if (CollectionUtils.isEmpty(deptList)) {
            return;
        }
        deptList.forEach(x -> {
            resultIds.add(x.getDeptId());
            recursiveDownGetDeptIds(deptMap, deptMap.get(x.getDeptId()), resultIds);
        });
    }
}
