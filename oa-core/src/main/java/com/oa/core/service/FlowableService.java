package com.oa.core.service;

import com.oa.core.enums.AuditTypeEnum;
import com.oa.core.model.dto.FlowableAuditParam;
import com.oa.core.model.dto.FlowableTaskCallbackParam;
import com.oa.core.model.dto.FlowableTaskTransferParam;
import com.oa.core.model.vo.AuditNodeRecordVO;
import org.flowable.task.api.Task;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FlowableService {

    /**
     * 部署
     *
     * @param fileName   文件名称
     * @param deployName 发布名称
     * @param deployKey  发布Key
     */
    void deploy(String fileName, String deployName, String deployKey);

    /**
     * 开启流程
     *
     * @param bizId         业务ID
     * @param auditTypeEnum 业务和流程KEY关联关系
     * @return 流程实例ID
     */
    String startProcess(Long bizId, AuditTypeEnum auditTypeEnum);

    /**
     * 审批
     *
     * @param param 业务信息
     */
    void audit(FlowableAuditParam param);

    /**
     * 转交
     *
     * @param param 业务信息
     */
    void transfer(FlowableTaskTransferParam param);

    /**
     * 回退
     *
     * @param param 业务信息
     */
    void rollback(FlowableTaskCallbackParam param);

    /**
     * 查询待我审批
     *
     * @param userId
     * @return
     */
    List<String> selectPendingApprovalByUser(Long userId);

    /**
     * 查询抄送我的
     *
     * @param userId
     * @return
     */
    List<String> selectSendMeByUser(Long userId);

    /**
     * 查询待我已审批数据
     *
     * @param userId
     * @return
     */
    List<String> selectApprovedByUser(Long userId);


    List<AuditNodeRecordVO> selectAllNodeInfo(String instanceId);

    Map<String, Object> selectCurrentTaskCandidateUser(String instanceId);

    /**
     * 催办
     *
     * @param id        业务ID
     * @param auditType 业务类型
     */
    void remind(Long id, Integer auditType);

    Task selectCurrentTask(String instanceId);

    /**
     * 调用流程在重新提交之后
     *
     * @param id            业务ID
     * @param auditTypeEnum 业务类型
     * @param remark        备注
     */
    void invokeProcessResubmitAfter(Long id, AuditTypeEnum auditTypeEnum, String remark);

    /**
     * 删除流程
     *
     * @param auditTypeEnum 业务类型
     * @param bizId         业务ID
     */
    void delProcess(AuditTypeEnum auditTypeEnum, Long bizId);

    /**
     * 删除流程
     *
     * @param auditTypeEnum 业务类型
     * @param bizIds        业务ID
     */
    void batchDelProcess(AuditTypeEnum auditTypeEnum, Collection<Long> bizIds);
}
