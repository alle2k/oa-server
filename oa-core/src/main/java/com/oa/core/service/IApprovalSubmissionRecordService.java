package com.oa.core.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.core.domain.ApprovalSubmissionRecord;
import com.oa.core.enums.DeletedEnum;

public interface IApprovalSubmissionRecordService extends IService<ApprovalSubmissionRecord> {

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
}
