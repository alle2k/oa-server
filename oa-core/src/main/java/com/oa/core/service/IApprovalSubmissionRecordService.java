package com.oa.core.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.core.domain.ApprovalSubmissionRecord;
import com.oa.core.enums.DeletedEnum;
import com.oa.core.model.dto.ApprovalSubmissionRecordSaveDto;
import com.oa.core.model.vo.BizDetailVo;
import com.oa.core.model.vo.BizInfoBaseVo;

public interface IApprovalSubmissionRecordService extends IService<ApprovalSubmissionRecord> {

    /**
     * 根据流程实例ID获取审批提交记录
     *
     * @param instanceId 实例ID
     * @return ApprovalSubmissionRecord
     */
    default ApprovalSubmissionRecord selectByInstanceId(String instanceId) {
        return getOne(Wrappers.<ApprovalSubmissionRecord>lambdaQuery()
                .eq(ApprovalSubmissionRecord::getInstanceId, instanceId)
                .eq(ApprovalSubmissionRecord::getDeleted, DeletedEnum.UN_DELETE.getCode()));
    }

    /**
     * 根据审批编号获取审批提交记录
     *
     * @param auditNo 审批编号
     * @return ApprovalSubmissionRecord
     */
    default ApprovalSubmissionRecord selectByAuditNo(String auditNo) {
        return getOne(Wrappers.<ApprovalSubmissionRecord>lambdaQuery()
                .eq(ApprovalSubmissionRecord::getAuditNo, auditNo)
                .eq(ApprovalSubmissionRecord::getDeleted, DeletedEnum.UN_DELETE.getCode()));
    }

    /**
     * 保存
     *
     * @param record 业务信息
     * @return 审批编号
     */
    String add(ApprovalSubmissionRecordSaveDto record);


    /**
     * 根据ID获取业务详情
     *
     * @param bizId     PK
     * @param auditType 审批类型
     * @return BizDetailVo
     * @see com.oa.core.enums.ApprovalSubmissionRecordStatusEnum
     */
    BizDetailVo<BizInfoBaseVo> getBizDetailByBizIdAndAuditType(Long bizId, Integer auditType);
}
