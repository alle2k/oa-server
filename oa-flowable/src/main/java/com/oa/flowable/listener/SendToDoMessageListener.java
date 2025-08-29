package com.oa.flowable.listener;

import com.oa.flowable.enums.CandidateTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

@Slf4j
public class SendToDoMessageListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        execute(delegateTask, CandidateTypeEnum.AUDIT);
    }

    /**
     * 获取需要发送通知的用户ID集合
     *
     * @param delegateTask 任务信息
     * @param typeEnum     候选人类型
     */
    public static void execute(DelegateTask delegateTask, CandidateTypeEnum typeEnum) {
//        String id = delegateTask.getId();
//        Set<IdentityLink> candidates = delegateTask.getCandidates();
//        if (CollectionUtils.isEmpty(candidates)) {
//            log.info("当前任务无候选信息，taskId：{}，key：{}", id, delegateTask.getTaskDefinitionKey());
//            return;
//        }
//        Set<Long> needSendMsgUserIdSet = new HashSet<>();
//        candidates.stream().map(IdentityLink::getUserId).filter(StringUtils::isNotBlank).forEach(userXml -> {
//            Document document = XmlUtil.parseXml(userXml);
//            NodeList nodeList = document.getElementsByTagName("type");
//            if (Objects.isNull(nodeList)) {
//                log.error("候选人设置异常，未找到<type>标签，userId：{}", userXml);
//                return;
//            }
//            String type = nodeList.item(0).getTextContent();
//            if (!typeEnum.getValue().equals(Integer.valueOf(type))) {
//                return;
//            }
//            nodeList = document.getElementsByTagName("userId");
//            if (Objects.isNull(nodeList)) {
//                log.error("候选人设置异常，未找到<userId>标签，userId：{}", userXml);
//                return;
//            }
//            needSendMsgUserIdSet.add(Long.valueOf(nodeList.item(0).getTextContent()));
//        });
//        if (CollectionUtils.isEmpty(needSendMsgUserIdSet) && typeEnum != CandidateTypeEnum.CC) {
//            log.info("当前任务无候选人，不发送通知，taskId：{}，key：{}", id, delegateTask.getTaskDefinitionKey());
//            return;
//        }
//        HistoricProcessInstance historicProcessInstance = SpringUtils.getBean(HistoryService.class)
//                .createHistoricProcessInstanceQuery().processInstanceId(delegateTask.getProcessInstanceId()).singleResult();
//        AuditRecord auditRecord;
//        IAuditBizProcessor auditBizProcessor;
//        Object bizTypeObj = delegateTask.getTransientVariable("bizType");
//        Object bizIdObj = delegateTask.getTransientVariable("bizId");
//        String bizKey, no = StringUtils.EMPTY;
//        Integer bizType;
//        if (!Objects.isNull(bizTypeObj) && !Objects.isNull(bizIdObj)) {
//            bizType = (Integer) delegateTask.getTransientVariable("bizType");
//            bizKey = String.valueOf(delegateTask.getTransientVariable("bizId"));
//            auditBizProcessor = AuditTypeEnum.getProcessorBean(bizType);
//        } else {
//            IApprovalSubmissionRecordService approvalSubmissionRecordService = SpringUtils.getBean(IApprovalSubmissionRecordService.class);
//            ApprovalSubmissionRecord approvalSubmissionRecord = approvalSubmissionRecordService.selectByInstanceId(delegateTask.getProcessInstanceId());
//            if (Objects.isNull(approvalSubmissionRecord)) {
//                throw new BaseException(BaseCode.DATA_NOT_EXIST.getCode(), "审批提交记录不存在");
//            }
//            auditBizProcessor = AuditTypeEnum.getProcessorBean(approvalSubmissionRecord.getAuditType());
//            bizType = approvalSubmissionRecord.getAuditType();
//            bizKey = String.valueOf(approvalSubmissionRecord.getBizId());
//            no = approvalSubmissionRecord.getAuditNo();
//        }
//        User user = SpringCtxUtils.getBean(IUserService.class).getById(historicProcessInstance.getStartUserId());
//        auditRecord = AuditRecord.builder()
//                .bizKey(bizKey).auditId(bizType).no(no)
//                .createUser(Integer.valueOf(historicProcessInstance.getStartUserId()))
//                .createTime(historicProcessInstance.getStartTime()).build();
//        if (Objects.isNull(user)) {
//            throw new BaseException(BaseCode.DATA_NOT_EXIST.getCode(), "发起人不存在");
//        }
//        if (typeEnum == CandidateTypeEnum.AUDIT) {
//            auditBizProcessor.sendWaitApprovalMessage(auditRecord, user, needSendMsgUserIdSet, AuditTypeEnum.getByCode(bizType));
//            return;
//        }
//        String auditResult = delegateTask.getVariable(FlowableConstants.AUDIT_VAR_NAME, String.class);
//        String title = AuditTypeEnum.getDescByCode(bizType) + ProcessStatusEnum.getByCode(auditResult).getMsg();
//        auditBizProcessor.sendFinishedMessage(auditRecord, title, needSendMsgUserIdSet, Boolean.FALSE);
    }
}
