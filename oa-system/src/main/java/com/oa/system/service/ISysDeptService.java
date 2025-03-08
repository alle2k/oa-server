package com.oa.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.common.core.domain.TreeSelect;
import com.oa.common.core.domain.entity.SysDept;
import com.oa.common.exception.ServiceException;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 部门管理 服务层
 */
public interface ISysDeptService extends IService<SysDept> {
    /**
     * 查询部门管理数据
     *
     * @param dept 部门信息
     * @return 部门信息集合
     */
    List<SysDept> selectDeptList(SysDept dept);

    /**
     * 查询部门树结构信息
     *
     * @param dept 部门信息
     * @return 部门树信息集合
     */
    List<TreeSelect> selectDeptTreeList(SysDept dept);

    /**
     * 构建前端所需要树结构
     *
     * @param depts 部门列表
     * @return 树结构列表
     */
    List<SysDept> buildDeptTree(List<SysDept> depts);

    /**
     * 构建前端所需要下拉树结构
     *
     * @param depts 部门列表
     * @return 下拉树结构列表
     */
    List<TreeSelect> buildDeptTreeSelect(List<SysDept> depts);

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    List<Long> selectDeptListByRoleId(Long roleId);

    /**
     * 根据部门ID查询信息
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    SysDept selectDeptById(Long deptId);

    /**
     * 根据ID查询所有子部门（正常状态）
     *
     * @param deptId 部门ID
     * @return 子部门数
     */
    int selectNormalChildrenDeptById(Long deptId);

    /**
     * 是否存在部门子节点
     *
     * @param deptId 部门ID
     * @return 结果
     */
    boolean hasChildByDeptId(Long deptId);

    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    boolean checkDeptExistUser(Long deptId);

    /**
     * 校验部门名称是否唯一
     *
     * @param dept 部门信息
     * @return 结果
     */
    boolean checkDeptNameUnique(SysDept dept);

    /**
     * 校验部门是否有数据权限
     *
     * @param deptId 部门id
     */
    void checkDeptDataScope(Long deptId);

    /**
     * 新增保存部门信息
     *
     * @param dept 部门信息
     * @return 结果
     */
    int insertDept(SysDept dept);

    /**
     * 修改保存部门信息
     *
     * @param dept 部门信息
     * @return 结果
     */
    int updateDept(SysDept dept);

    /**
     * 删除部门管理信息
     *
     * @param deptId 部门ID
     * @return 结果
     */
    int deleteDeptById(Long deptId);

    /**
     * 根据部门ID获取部门详情
     *
     * @param deptId 部门ID
     * @return SysDept
     */
    default SysDept selectOneByDeptId(Long deptId) {
        SysDept entity = getById(deptId);
        if (Objects.isNull(entity)) {
            throw new ServiceException("部门不存在");
        }
        return entity;
    }

    /**
     * 递归获取所有部门领导
     *
     * @param deptIds 部门ID列表
     * @return List
     */
    Set<Long> recursiveGetDeptLeader(Collection<Long> deptIds);

    /**
     * 根据部门ID获取该部门下所有用户ID集合
     *
     * @param deptId 部门ID
     * @return 该部门下的所有用户ID集合
     */
    Set<Long> fetchDeptUserIds(Long deptId);

    /**
     * 递归获取指定部门及其所有子部门的ID集合。
     *
     * @param deptId 指定的部门ID
     * @return 包含指定部门及其所有子部门ID的集合
     */
    Set<Long> recursiveDownGetDeptIds(Long deptId);
}
