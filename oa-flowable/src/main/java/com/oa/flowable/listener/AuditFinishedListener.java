package com.oa.flowable.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

@Slf4j
public class AuditFinishedListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        execute(delegateTask, Boolean.TRUE);
    }

    public static void execute(DelegateTask delegateTask, Boolean onlySendPassedMsgFlag) {
//        String auditResult = delegateTask.getVariable(FlowableConstants.AUDIT_VAR_NAME, String.class);
//        if ((!onlySendPassedMsgFlag && auditResult.toLowerCase().contains(ProcessStatusEnum.COMPLETE.getCode())) ||
//                (onlySendPassedMsgFlag && auditResult.equals(ProcessStatusEnum.REJECT.getCode()))) {
//            return;
//        }
//        IApprovalSubmissionRecordService approvalSubmissionRecordService = SpringCtxUtils.getBean(IApprovalSubmissionRecordService.class);
//        ApprovalSubmissionRecord approvalSubmissionRecord = approvalSubmissionRecordService.selectByInstanceId(delegateTask.getProcessInstanceId());
//        if (Objects.isNull(approvalSubmissionRecord)) {
//            throw new BaseException(BaseCode.DATA_NOT_EXIST.getCode(), "审批提交记录不存在");
//        }
//        Long bizId = approvalSubmissionRecord.getBizId();
//        IAuditBizProcessor auditBizProcessor = AuditTypeEnum.getProcessorBean(approvalSubmissionRecord.getAuditType());
//        HistoricProcessInstance historicProcessInstance = SpringCtxUtils.getBean(HistoryService.class)
//                .createHistoricProcessInstanceQuery().processInstanceId(delegateTask.getProcessInstanceId()).singleResult();
//        AuditRecord auditRecord = AuditRecord.builder()
//                .bizKey(String.valueOf(bizId)).auditId(approvalSubmissionRecord.getAuditType()).no(approvalSubmissionRecord.getAuditNo())
//                .createUser(Integer.valueOf(historicProcessInstance.getStartUserId()))
//                .createTime(historicProcessInstance.getStartTime()).build();
//        List<Long> ccUserIdList = new LinkedList<>();
//        delegateTask.getCandidates().stream().map(IdentityLinkInfo::getUserId).forEach(x -> {
//            if (StringUtils.isBlank(x)) {
//                return;
//            }
//            Document document = XmlUtil.parseXml(x);
//            String type = FlowableServiceImpl.parseXmlTargetContent(document, "type");
//            if (StringUtils.isBlank(type)) {
//                log.error("候选人设置异常，未找到<type>标签，userId：{}", x);
//                return;
//            }
//            if (Integer.valueOf(type).equals(CandidateTypeEnum.CC.getValue())) {
//                String userId = FlowableServiceImpl.parseXmlTargetContent(document, "userId");
//                if (StringUtils.isBlank(userId)) {
//                    log.error("候选人设置异常，未找到<userId>标签，userId：{}", x);
//                    return;
//                }
//                ccUserIdList.add(Long.valueOf(userId));
//            }
//        });
//        if (auditResult.equals(ProcessStatusEnum.REJECT.getCode())) {
//            auditBizProcessor.whenReject(auditRecord, ccUserIdList);
//            return;
//        }
//        auditBizProcessor.whenPass(auditRecord, ccUserIdList);
    }
}
