package com.oa.flowable.mapper.flowable;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @auther CodeGenerator
 * @create 2023-08-29 09:41:45
 * @describe 审批流程mapper类
 */
public interface AuditMapper {

    List<String> getCommentInfoByTaskId(@Param("taskId") String taskId);

    List<String> getCandidateProcInstId(@Param("user") String user);
}
