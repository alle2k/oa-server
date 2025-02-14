package com.oa.core.listener;

import com.oa.common.exception.ServiceException;
import com.oa.common.utils.spring.SpringUtils;
import com.oa.core.domain.ApprovalSubmissionRecord;
import com.oa.core.enums.AuditTypeEnum;
import com.oa.core.processor.AbstractAuditBizProcessor;
import com.oa.core.service.IApprovalSubmissionRecordService;
import com.oa.flowable.constants.FlowableConstants;
import com.oa.flowable.enums.ProcessStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

import java.util.Objects;

@Slf4j
public class AuditFinishedListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        execute(delegateTask);
    }

    public static void execute(DelegateTask delegateTask) {
        String auditResult = delegateTask.getVariable(FlowableConstants.AUDIT_VAR_NAME, String.class);
        IApprovalSubmissionRecordService approvalSubmissionRecordService = SpringUtils.getBean(IApprovalSubmissionRecordService.class);
        ApprovalSubmissionRecord approvalSubmissionRecord = approvalSubmissionRecordService.selectByInstanceId(delegateTask.getProcessInstanceId());
        if (Objects.isNull(approvalSubmissionRecord)) {
            throw new ServiceException("审批提交记录不存在");
        }
        Long bizId = approvalSubmissionRecord.getBizId();
        AbstractAuditBizProcessor auditBizProcessor = AuditTypeEnum.getProcessorBean(approvalSubmissionRecord.getAuditType());
        if (auditResult.equals(ProcessStatusEnum.REJECT.getCode())) {
            auditBizProcessor.whenReject(bizId);
            return;
        }
        auditBizProcessor.whenPass(bizId);
    }
}
