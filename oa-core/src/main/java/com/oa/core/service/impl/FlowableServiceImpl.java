package com.oa.core.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.XmlUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.oa.common.annotation.MultiTransactional;
import com.oa.common.constant.TransactionConstant;
import com.oa.common.core.domain.entity.SysUser;
import com.oa.common.core.domain.model.LoginUser;
import com.oa.common.core.redis.RedisCache;
import com.oa.common.error.BaseCode;
import com.oa.common.exception.ServiceException;
import com.oa.common.utils.SecurityUtils;
import com.oa.common.utils.bean.BeanValidators;
import com.oa.core.config.FlowableSpecialApprovalConfig;
import com.oa.core.domain.ApprovalSubmissionRecord;
import com.oa.core.domain.TaskTransferLog;
import com.oa.core.enums.ApprovalSubmissionRecordStatusEnum;
import com.oa.core.enums.AuditTypeEnum;
import com.oa.core.enums.FlowableTaskCallbackOperateTypeEnum;
import com.oa.core.model.dto.*;
import com.oa.core.model.vo.AuditNodeRecordVO;
import com.oa.core.model.vo.NodeCandidateInfoVO;
import com.oa.core.model.vo.NodeCommentVO;
import com.oa.core.processor.AbstractAuditBizProcessor;
import com.oa.core.service.FlowableService;
import com.oa.core.service.IApprovalSubmissionRecordService;
import com.oa.core.service.ITaskTransferLogService;
import com.oa.flowable.constants.FlowableConstants;
import com.oa.flowable.enums.CandidateTypeEnum;
import com.oa.flowable.enums.ProcessStatusEnum;
import com.oa.flowable.mapper.flowable.AuditMapper;
import com.oa.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.annotation.Resource;
import javax.validation.Validator;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FlowableServiceImpl implements FlowableService {

    @Value("${oa.bpmnPath}")
    private String bpmnPath;

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;
    @Resource
    private TaskService taskService;
    @Resource
    private IApprovalSubmissionRecordService approvalSubmissionRecordService;
    @Resource
    private RedisCache redisCache;
    @Resource
    protected Validator validator;
    @Resource
    private ITaskTransferLogService taskTransferLogService;
    @Resource
    private AuditMapper auditMapper;
    @Resource
    private ISysUserService sysUserService;

    @Override
    public void deploy(String fileName, String deployName, String deployKey) {
        try (InputStream fis = Files.newInputStream(Paths.get(bpmnPath + File.separator + fileName))) {
            Deployment deploy = repositoryService.createDeployment()
                    .addInputStream(fileName, fis).name(deployName).key(deployKey).deploy();
            log.info("部署成功:{}", deploy.getId());
        } catch (Exception e) {
            log.error("发布失败", e);
            throw new ServiceException(BaseCode.SYSTEM_FAILED);
        }
    }

    @Transactional(TransactionConstant.FLOWABLE)
    @Override
    public String startProcess(Long bizId, AuditTypeEnum auditTypeEnum) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long id = loginUser.getUserId();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(auditTypeEnum.getProcessDefinitionKey()).latestVersion().singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        StartEvent flowElement = (StartEvent) bpmnModel.getFlowElement(auditTypeEnum.getProcessDefinitionKey() + "StartEvent");
        String initiator = flowElement.getInitiator();
        if (!loginUser.getUser().isAdmin() && !StringUtils.isBlank(initiator) &&
                Arrays.stream(initiator.split(",")).map(Long::valueOf).noneMatch(x -> x.equals(id))) {
            throw new ServiceException("用户无权发起审批");
        }
        log.info("=========开启流程，userId：{}=========", id);
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("bizId", bizId);
        variableMap.put("bizType", auditTypeEnum.getCode());
        Authentication.setAuthenticatedUserId(String.valueOf(id));
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(auditTypeEnum.getProcessDefinitionKey(), variableMap);
        log.info("=========流程已开启，实例ID：{}=========", processInstance.getId());
        return processInstance.getId();
    }

    @MultiTransactional
    @Override
    public void audit(FlowableAuditParam param) {
        ProcessStatusEnum statusEnum = ProcessStatusEnum.codeMap.get(param.getAuditAction());
        if (Objects.isNull(statusEnum)) {
            throw new ServiceException(BaseCode.PARAM_ERROR);
        }
        if (statusEnum != ProcessStatusEnum.REJECT && statusEnum != ProcessStatusEnum.COMPLETE) {
            throw new ServiceException(BaseCode.PARAM_ERROR);
        }
        FlowableCommentParam comment = param.getComment();
        boolean commentIsNullFlag = Objects.isNull(comment);
        if (statusEnum == ProcessStatusEnum.REJECT && (commentIsNullFlag || StringUtils.isBlank(comment.getRemark()))) {
            throw new ServiceException("请填写拒绝原因");
        }
        AuditTypeEnum auditTypeEnum = AuditTypeEnum.codeMap.get(param.getAuditType());
        String procInstId = getProcInstIdByBizIdAndType(param.getId(), auditTypeEnum);
        ProcessInstance processInstance = getProcessInstanceByProcInstId(procInstId);
        List<Task> taskList = getActiveTaskListByProcInstId(procInstId);
        String commentStr = StringUtils.EMPTY;
        if (!commentIsNullFlag) {
            commentStr = JSON.toJSONString(comment);
        }
        HashMap<String, Object> variables = new HashMap<>();
        variables.put(FlowableConstants.AUDIT_VAR_NAME, param.getAuditAction());
        if (statusEnum == ProcessStatusEnum.COMPLETE) {
            Class<?> clazz = FlowableSpecialApprovalConfig.SPECIAL_APPROVAL_FORM_MAP.get(taskList.get(0).getTaskDefinitionKey());
            if (!Objects.isNull(clazz)) {
                String extra = param.getExtra();
                if (StringUtils.isBlank(extra)) {
                    throw new ServiceException(BaseCode.PARAM_ERROR);
                }
                Object obj = JSON.parseObject(extra, clazz);
                BeanValidators.validateWithException(validator, obj);
                auditTypeEnum.getProcessorBean().invoke(param.getId(), obj);
            }
            variables.put(FlowableConstants.AUDIT_VAR_NAME, statusEnum.getCode());
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        String userId = getUserIdXmlByUserId(loginUser.getUserId());
        boolean flag = false;
        Authentication.setAuthenticatedUserId(userId);
        Iterator<Task> iterator = taskList.iterator();
        do {
            Task task = iterator.next();
            // TODO 超管这里应该是再需要特殊处理的，总不能点个审批把所有的实例任务都批完了吧
            if (user.isAdmin()) {
                flag = true;
                taskService.claim(task.getId(), userId);
                if (!commentIsNullFlag) {
                    taskService.addComment(task.getId(), processInstance.getProcessInstanceId(), commentStr);
                }
                taskService.complete(task.getId(), variables);
                continue;
            }
            if (checkTaskContainsUserId(task.getId(), userId)) {
                continue;
            }
            flag = true;
            taskService.claim(task.getId(), userId);
            if (!commentIsNullFlag) {
                taskService.addComment(task.getId(), processInstance.getProcessInstanceId(), commentStr);
            }
            taskService.complete(task.getId(), variables);
        } while (iterator.hasNext());
        if (!flag) {
            throw new ServiceException("用户无权审批任务");
        }
        HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId()).variableName(FlowableConstants.PROCESS_STATUS).singleResult();
        ProcessStatusEnum processStatusEnum = ProcessStatusEnum.codeMap.get((String) historicVariableInstance.getValue());
        approvalSubmissionRecordService.update(Wrappers.<ApprovalSubmissionRecord>lambdaUpdate()
                .set(ApprovalSubmissionRecord::getApprovalStatus, getApprovalSubmissionRecordStatusEnum(processStatusEnum).getCode())
                .set(ApprovalSubmissionRecord::getUpdateUser, loginUser.getUserId())
                .set(ApprovalSubmissionRecord::getApprovalTime, new Date())
                .eq(ApprovalSubmissionRecord::getBizId, param.getId())
                .eq(ApprovalSubmissionRecord::getAuditType, param.getAuditType()));
    }

    private ApprovalSubmissionRecordStatusEnum getApprovalSubmissionRecordStatusEnum(ProcessStatusEnum processStatusEnum) {
        ApprovalSubmissionRecordStatusEnum approvalSubmissionRecordStatusEnum;
        if (processStatusEnum == ProcessStatusEnum.REJECT) {
            approvalSubmissionRecordStatusEnum = ApprovalSubmissionRecordStatusEnum.REJECT;
        } else if (processStatusEnum.getCode().toLowerCase().contains(ProcessStatusEnum.COMPLETE.getCode())) {
            approvalSubmissionRecordStatusEnum = ApprovalSubmissionRecordStatusEnum.PASS;
        } else {
            approvalSubmissionRecordStatusEnum = ApprovalSubmissionRecordStatusEnum.AUDIT;
        }
        return approvalSubmissionRecordStatusEnum;
    }

    @MultiTransactional
    @Override
    public void transfer(FlowableTaskTransferParam param) {
        String procInstId = getProcInstIdByBizIdAndType(param.getId(), AuditTypeEnum.codeMap.get(param.getAuditType()));
        if (StringUtils.isBlank(procInstId)) {
            throw new ServiceException("流程实例不存在");
        }
        ProcessInstance processInstance = getProcessInstanceByProcInstId(procInstId);
        List<Task> taskList = getActiveTaskListByProcInstId(procInstId);
        String userId = getUserIdXmlByUserId(SecurityUtils.getUserId());
        String targetUserId = getUserIdXmlByUserId(param.getTargetUserId());
        FlowableCommentParam comment = param.getComment();
        if (Objects.isNull(comment)) {
            comment = new FlowableCommentParam();
            comment.setTransferTo(targetUserId);
        }
        String commentStr = JSON.toJSONString(comment);
        boolean flag = false;
        Iterator<Task> iterator = taskList.iterator();
        do {
            Task task = iterator.next();
            if (checkTaskContainsUserId(task.getId(), userId)) {
                continue;
            }
            flag = true;
            taskService.claim(task.getId(), userId);
            taskService.unclaim(task.getId());
            addTargetUserAndDelOtherCandidateUsers(task.getId(), targetUserId);
            taskService.claim(task.getId(), targetUserId);
            Authentication.setAuthenticatedUserId(userId);
            taskService.addComment(task.getId(), processInstance.getProcessInstanceId(), commentStr);
            TaskTransferLog taskTransferLog = new TaskTransferLog();
            taskTransferLog.setTaskId(task.getId());
            taskTransferLog.setInstanceId(processInstance.getProcessInstanceId());
            taskTransferLog.setOriginalAssignee(SecurityUtils.getUserId());
            taskTransferLog.setTargetAssignee(param.getTargetUserId());
            taskTransferLog.setOperationType(1);
            taskTransferLog.setCreateUser(SecurityUtils.getUserId());
            taskTransferLog.setReviewData(commentStr);
            taskTransferLogService.save(taskTransferLog);
        } while (iterator.hasNext());
        if (!flag) {
            throw new ServiceException("用户无权转交任务");
        }
    }

    @MultiTransactional
    @Override
    public void rollback(FlowableTaskCallbackParam param) {
        AuditTypeEnum auditTypeEnum = AuditTypeEnum.codeMap.get(param.getAuditType());
        String procInstId = getProcInstIdByBizIdAndType(param.getId(), auditTypeEnum);
        if (StringUtils.isBlank(procInstId)) {
            throw new ServiceException("流程实例不存在");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userId = loginUser.getUserId();
        ProcessInstance processInstance = getProcessInstanceByProcInstId(procInstId);
        List<Task> taskList = getActiveTaskListByProcInstId(procInstId);

        Integer operateType = param.getOperateType();
        ApprovalSubmissionRecordStatusEnum statusEnum = ApprovalSubmissionRecordStatusEnum.ROLLBACK;
        if (FlowableTaskCallbackOperateTypeEnum.CANCEL.getValue().equals(operateType)) {
            statusEnum = ApprovalSubmissionRecordStatusEnum.CANCEL;
        }
        AuditTypeEnum.getProcessorBean(param.getAuditType()).whenRevoke(param.getId(), statusEnum);
        if (FlowableTaskCallbackOperateTypeEnum.CANCEL.getValue().equals(operateType)) {
            String startUserId = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getProcessInstanceId())
                    .unfinished().singleResult().getStartUserId();
            if (StringUtils.isBlank(startUserId)) {
                throw new ServiceException("发起人不存在");
            }
            if (!userId.equals(Long.valueOf(startUserId))) {
                throw new ServiceException("当前用户不是发起人，不准撤销");
            }
            runtimeService.deleteProcessInstance(procInstId, "发起人撤销，流程结束");
            approvalSubmissionRecordService.update(Wrappers.<ApprovalSubmissionRecord>lambdaUpdate()
                    .set(ApprovalSubmissionRecord::getApprovalStatus, ApprovalSubmissionRecordStatusEnum.CANCEL.getCode())
                    .set(ApprovalSubmissionRecord::getApprovalTime, new Date())
                    .set(ApprovalSubmissionRecord::getUpdateUser, userId)
                    .eq(ApprovalSubmissionRecord::getBizId, param.getId())
                    .eq(ApprovalSubmissionRecord::getAuditType, param.getAuditType()));
            //更新任务日志表
            taskList.forEach(x -> {
                TaskTransferLog taskTransferLog = new TaskTransferLog();
                taskTransferLog.setTaskId(x.getId());
                taskTransferLog.setInstanceId(processInstance.getProcessInstanceId());
                taskTransferLog.setOperationType(3);
                taskTransferLog.setCreateUser(userId);
                taskTransferLogService.save(taskTransferLog);
            });
            return;
        }
        boolean flag = false;
        String userIdXml = getUserIdXmlByUserId(userId);
        FlowableCommentParam comment = param.getComment();
        String commentStr = StringUtils.EMPTY;
        if (!Objects.isNull(comment)) {
            commentStr = JSON.toJSONString(comment);
        }
        Authentication.setAuthenticatedUserId(userIdXml);
        Iterator<Task> iterator = taskList.iterator();
        do {
            Task task = iterator.next();

            // 记录任务退回日志
            TaskTransferLog rollbackLog = new TaskTransferLog();
            rollbackLog.setTaskId(task.getId());
            rollbackLog.setInstanceId(processInstance.getProcessInstanceId());
            rollbackLog.setOperationType(2);
            rollbackLog.setCreateUser(userId);
            rollbackLog.setReviewData(commentStr);

            if (loginUser.getUser().isAdmin()) {
                flag = true;
                taskService.addComment(task.getId(), processInstance.getProcessInstanceId(), commentStr);
                taskTransferLogService.save(rollbackLog);
                runtimeService.createChangeActivityStateBuilder()
                        .processInstanceId(processInstance.getProcessInstanceId())
                        .moveExecutionToActivityId(task.getExecutionId(), auditTypeEnum.getProcessDefinitionKey() + "ReSubmit")
                        .changeState();
                continue;
            }
            if (checkTaskContainsUserId(task.getId(), userIdXml)) {
                continue;
            }
            flag = true;
            taskService.addComment(task.getId(), processInstance.getProcessInstanceId(), commentStr);
            taskTransferLogService.save(rollbackLog);
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(processInstance.getProcessInstanceId())
                    .moveExecutionToActivityId(task.getExecutionId(), auditTypeEnum.getProcessDefinitionKey() + "ReSubmit")
                    .changeState();
        } while (iterator.hasNext());
        if (!flag) {
            throw new ServiceException("用户无权退回");
        }
        approvalSubmissionRecordService.update(Wrappers.<ApprovalSubmissionRecord>lambdaUpdate()
                .set(ApprovalSubmissionRecord::getApprovalStatus, ApprovalSubmissionRecordStatusEnum.ROLLBACK.getCode())
                .set(ApprovalSubmissionRecord::getApprovalTime, new Date())
                .set(ApprovalSubmissionRecord::getUpdateUser, userId)
                .eq(ApprovalSubmissionRecord::getBizId, param.getId())
                .eq(ApprovalSubmissionRecord::getAuditType, param.getAuditType()));
    }

    /**
     * 获取流程实例ID
     *
     * @param bizId         业务ID
     * @param auditTypeEnum 业务类型
     * @return 流程实例ID
     */
    private String getProcInstIdByBizIdAndType(Long bizId, AuditTypeEnum auditTypeEnum) {
        if (Objects.isNull(auditTypeEnum)) {
            throw new ServiceException(BaseCode.PARAM_ERROR);
        }
        AbstractAuditBizProcessor processorBean = AuditTypeEnum.getProcessorBean(auditTypeEnum.getCode());
        ApprovalSubmissionRecord entity = approvalSubmissionRecordService.selectByAuditNo(processorBean.getAuditNoByBizId(bizId));
        if (Objects.isNull(entity) || StringUtils.isBlank(entity.getInstanceId())) {
            return StringUtils.EMPTY;
        }
        return entity.getInstanceId();
    }

    /**
     * 根据ID获取流程实例
     *
     * @param procInstId 流程实例ID
     * @return 流程实例
     */
    private ProcessInstance getProcessInstanceByProcInstId(String procInstId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(procInstId).singleResult();
        if (Objects.isNull(processInstance)) {
            log.error("流程实例不存在，流程ID：{}", procInstId);
            throw new ServiceException("流程实例不存在");
        }
        if (processInstance.isEnded()) {
            log.error("流程实例已结束，流程ID：{}", procInstId);
            throw new ServiceException("流程实例已结束");
        }
        return processInstance;
    }

    /**
     * 根据ID获取活动的任务列表
     *
     * @param procInstId 流程实例ID
     * @return 活动任务列表
     */
    private List<Task> getActiveTaskListByProcInstId(String procInstId) {
        List<Task> taskList = taskService.createTaskQuery()
                .processInstanceId(procInstId).active().list();
        if (CollectionUtils.isEmpty(taskList)) {
            log.error("未找到活动的任务，流程ID：{}", procInstId);
            throw new ServiceException("未找到活动的任务");
        }
        return taskList;
    }

    /**
     * 校验任务是否包含用户
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return boolean
     */
    private boolean checkTaskContainsUserId(String taskId, String userId) {
        List<IdentityLink> identityLinkList = taskService.getIdentityLinksForTask(taskId);
        if (CollectionUtils.isEmpty(identityLinkList)) {
            log.info("节点没有候选人，不进行处理，taskId：{}", taskId);
            return false;
        }
        return identityLinkList.stream().map(IdentityLink::getUserId).filter(StringUtils::isNotBlank)
                .noneMatch(x -> x.equals(userId));
    }

    private void addTargetUserAndDelOtherCandidateUsers(String taskId, String targetUserId) {
        List<IdentityLink> identityLinkList = taskService.getIdentityLinksForTask(taskId);
        if (CollectionUtils.isEmpty(identityLinkList)) {
            taskService.addCandidateUser(taskId, targetUserId);
            log.info("节点没有候选人，不进行处理，taskId：{}", taskId);
            return;
        }
        identityLinkList.stream().map(IdentityLink::getUserId).forEach(x -> {
            String type = parseXmlTargetContent(x, "type");
            if (StringUtils.isBlank(type)) {
                log.error("候选人设置异常，未找到<type>标签，type：{}", x);
                return;
            }
            if (Integer.valueOf(type).equals(CandidateTypeEnum.CC.getValue())) {
                return;
            }
            taskService.deleteCandidateUser(taskId, x);
        });
        taskService.addCandidateUser(taskId, targetUserId);
    }

    public static String parseXmlTargetContent(Document document, String elementName) {
        NodeList nodeList = document.getElementsByTagName(elementName);
        if (Objects.isNull(nodeList)) {
            log.error("候选人设置异常，未找到<{}>标签", elementName);
            return StringUtils.EMPTY;
        }
        return nodeList.item(0).getTextContent();
    }

    public static String parseXmlTargetContent(String xmlStr, String elementName) {
        if (StringUtils.isBlank(xmlStr)) {
            return StringUtils.EMPTY;
        }
        return parseXmlTargetContent(XmlUtil.parseXml(xmlStr), elementName);
    }

    /**
     * 根据userId和候选人类型获取Xml格式的用户ID
     *
     * @param userId 用户ID
     * @return 用户ID
     */
    public static String getUserIdXmlByUserId(Long userId) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", userId);
        map.put("type", CandidateTypeEnum.AUDIT.getValue());
        return XmlUtil.mapToXmlStr(map);
    }

    @Override
    public List<String> selectPendingApprovalByUser(Long userId) {
        //构建用户对象
        Map<String, Object> candidate = new LinkedHashMap<>();
        candidate.put("userId", userId);
        candidate.put("type", 0);
        //待我申领的任务
        List<Task> taskList = taskService.createTaskQuery()
                .taskCandidateUser(XmlUtil.mapToXmlStr(candidate))
                .list();
        List<Task> assigneeTask = taskService.createTaskQuery()
                .taskAssignee(XmlUtil.mapToXmlStr(candidate))
                .list();
        taskList.addAll(assigneeTask);
        List<String> instanceIds = new LinkedList<>();
        for (Task task : taskList) {
            String processInstanceId = task.getProcessInstanceId();
            instanceIds.add(processInstanceId);
        }
        return instanceIds;
    }

    @Override
    public List<String> selectSendMeByUser(Long userId) {
        Map<String, Object> candidate = new LinkedHashMap<>();
        candidate.put("userId", userId);
        candidate.put("type", CandidateTypeEnum.CC.getValue());
        return auditMapper.getCandidateProcInstId(XmlUtil.mapToXmlStr(candidate));
    }

    @Override
    public List<String> selectApprovedByUser(Long userId) {
        Map<String, Object> candidate = new LinkedHashMap<>();
        candidate.put("userId", userId);
        candidate.put("type", CandidateTypeEnum.AUDIT.getValue());
        List<HistoricActivityInstance> list = historyService
                .createHistoricActivityInstanceQuery()
                .finished()
                .taskAssignee(XmlUtil.mapToXmlStr(candidate))
                .list();
        List<String> instanceIds = new LinkedList<>();
        for (HistoricActivityInstance task : list) {
            String processInstanceId = task.getProcessInstanceId();
            String activityId = task.getActivityId();
            if (activityId.contains("ReSubmit")) {
                continue;
            }
            instanceIds.add(processInstanceId);
        }
        return instanceIds;
    }

    @Override
    public List<AuditNodeRecordVO> selectAllNodeInfo(String instanceId) {
        if (StringUtils.isBlank(instanceId)) {
            return Collections.emptyList();
        }
        //查询历史节点信息
        List<HistoricTaskInstance> historicTaskList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId)
                .orderByTaskCreateTime()
                .asc()
                .list();
        List<Long> userIds = new LinkedList<>();
        //封装开始节点信息
        List<NodeCandidateInfoVO> carbonCopyUsers = new LinkedList<>();
        List<AuditNodeRecordVO> resultNode = new LinkedList<>();
        ApprovalSubmissionRecord approvalSubmissionRecord = approvalSubmissionRecordService.getBaseMapper()
                .selectOne(new LambdaQueryWrapper<ApprovalSubmissionRecord>()
                        .eq(ApprovalSubmissionRecord::getInstanceId, instanceId));
        AuditNodeRecordVO startNode = new AuditNodeRecordVO();
        startNode.setNodeType(1);
        NodeCandidateInfoVO startCandidate = new NodeCandidateInfoVO();
        startCandidate.setUserId(approvalSubmissionRecord.getApplyUserId());
        List<NodeCandidateInfoVO> startNodeCandidateList = new LinkedList<>();
        startNodeCandidateList.add(startCandidate);
        startNode.setNodeCandidateInfo(startNodeCandidateList);
        startNode.setStartTime(approvalSubmissionRecord.getCreateTime());
        startNode.setNodeTime(approvalSubmissionRecord.getCreateTime());
        startNode.setCommentInfo(approvalSubmissionRecord.getRemark());
        userIds.add(approvalSubmissionRecord.getApplyUserId());
        resultNode.add(startNode);
        //封装用户任务节点信息
        for (HistoricTaskInstance historicTaskInstance : historicTaskList) {
            //查询任务记录表
            List<TaskTransferLog> taskTransferLogs = taskTransferLogService.list(new LambdaQueryWrapper<TaskTransferLog>()
                    .eq(TaskTransferLog::getInstanceId, instanceId)
                    .eq(TaskTransferLog::getTaskId, historicTaskInstance.getId())
                    .orderByAsc(TaskTransferLog::getCreateTime));
            String assignee = historicTaskInstance.getAssignee();
            Date endTime = historicTaskInstance.getEndTime();
            //任务正常流转
            AuditNodeRecordVO userNode = new AuditNodeRecordVO();
            boolean flag = false;
            if (CollectionUtil.isNotEmpty(taskTransferLogs)) {
                for (TaskTransferLog taskTransferLog : taskTransferLogs) {
                    AuditNodeRecordVO taskTransferNode = new AuditNodeRecordVO();
                    if (taskTransferLog.getOperationType() == 1) {
                        taskTransferNode.setNodeType(3);
                        TaskTransferInfoDto taskTransferInfoDto = new TaskTransferInfoDto();
                        taskTransferInfoDto.setOriginalAssignee(taskTransferLog.getOriginalAssignee());
                        taskTransferInfoDto.setTargetAssignee(taskTransferLog.getTargetAssignee());
                        taskTransferInfoDto.setTransferTime(taskTransferLog.getCreateTime());
                        taskTransferNode.setTaskTransferInfoDto(taskTransferInfoDto);
                        NodeCandidateInfoVO nodeCandidateInfoVO = new NodeCandidateInfoVO();
                        nodeCandidateInfoVO.setUserId(taskTransferLog.getOriginalAssignee());
                        userIds.add(taskTransferLog.getOriginalAssignee());
                        taskTransferNode.setNodeCandidateInfo(new LinkedList<>(Collections.singleton(nodeCandidateInfoVO)));
                    }
                    if (taskTransferLog.getOperationType() == 2) {
                        taskTransferNode.setNodeType(4);
                        NodeCandidateInfoVO nodeCandidateInfoVO = new NodeCandidateInfoVO();
                        nodeCandidateInfoVO.setUserId(taskTransferLog.getCreateUser());
                        taskTransferNode.setNodeCandidateInfo(new LinkedList<>(Collections.singleton(nodeCandidateInfoVO)));
                        userIds.add(taskTransferLog.getCreateUser());
                        flag = true;
                    }
                    if (taskTransferLog.getOperationType() == 3) {
                        taskTransferNode.setNodeType(5);
                        NodeCandidateInfoVO nodeCandidateInfoVO = new NodeCandidateInfoVO();
                        nodeCandidateInfoVO.setUserId(taskTransferLog.getCreateUser());
                        taskTransferNode.setNodeCandidateInfo(new LinkedList<>(Collections.singleton(nodeCandidateInfoVO)));
                        userIds.add(taskTransferLog.getCreateUser());
                        flag = true;
                    }
                    taskTransferNode.setNodeTime(taskTransferLog.getCreateTime());
                    String reviewData = taskTransferLog.getReviewData();
                    if (StringUtils.isNotBlank(reviewData)) {
                        NodeCommentVO nodeCommentVO = JSON.parseObject(reviewData, NodeCommentVO.class);
                        taskTransferNode.setCommentInfo(nodeCommentVO.getRemark());
                        taskTransferNode.setCommentAttachmentsUrl(nodeCommentVO.getAnnexUrl());
                        if (CollectionUtil.isEmpty(nodeCommentVO.getAnnexUrl())) {
                            taskTransferNode.setCommentAttachmentsUrl(Collections.emptyList());
                        }
                    }
                    taskTransferNode.setNodeName(historicTaskInstance.getName());
                    resultNode.add(taskTransferNode);
                }
            }
            if (flag) {
                continue;
            }
            userNode.setTaskKey(historicTaskInstance.getTaskDefinitionKey());
            Map<Integer, List<NodeCandidateInfoVO>> candidateTypeMap = getNodeUserByTaskIdAndCandidateType(historicTaskInstance.getId(), null)
                    .stream().collect(Collectors.groupingBy(NodeCandidateInfoVO::getCandidateType));
            if (endTime != null) {
                userNode.setNodeType(2);
                userNode.setNodeName(historicTaskInstance.getName());
                if (StringUtils.isNotBlank(assignee)) {
                    Map<String, Object> assigneeMap = XmlUtil.xmlToMap(assignee);
                    NodeCandidateInfoVO assigneeUser = new NodeCandidateInfoVO();
                    assigneeUser.setUserId(Long.valueOf(assigneeMap.get("userId").toString()));
                    userNode.setNodeCandidateInfo(new LinkedList<>(Collections.singleton(assigneeUser)));
                    userIds.add(assigneeUser.getUserId());
                } else {
                    userNode.setNodeCandidateInfo(candidateTypeMap.getOrDefault(CandidateTypeEnum.AUDIT.getValue(), Collections.emptyList()));
                }
                HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery()
                        .taskId(historicTaskInstance.getId())
                        .variableName(FlowableConstants.NODE_VAR_NAME)
                        .singleResult();
                if (Objects.nonNull(variableInstance)) {
                    String value = (String) variableInstance.getValue();
                    if (value.toLowerCase().contains(ProcessStatusEnum.COMPLETE.getCode())) {
                        userNode.setAuditStatus(1);
                    } else {
                        userNode.setAuditStatus(2);
                    }
                }
                //获取抄送人信息
                List<NodeCandidateInfoVO> carbonCopyIdByTaskId = candidateTypeMap.getOrDefault(CandidateTypeEnum.CC.getValue(), Collections.emptyList());
                carbonCopyUsers.addAll(carbonCopyIdByTaskId);
                if (CollectionUtil.isNotEmpty(carbonCopyIdByTaskId)) {
                    userIds.addAll(carbonCopyIdByTaskId.stream().map(NodeCandidateInfoVO::getUserId).collect(Collectors.toList()));
                }
                //设置附件信息
                List<String> commentInfos = auditMapper.getCommentInfoByTaskId(historicTaskInstance.getId());
                if (!CollectionUtils.isEmpty(commentInfos)) {
                    String s = commentInfos.get(commentInfos.size() - 1);
                    NodeCommentVO nodeCommentVO = JSON.parseObject(s, NodeCommentVO.class);
                    userNode.setCommentInfo(nodeCommentVO.getRemark());
                    List<String> annexUrl = nodeCommentVO.getAnnexUrl();
                    userNode.setCommentAttachmentsUrl(annexUrl);
                    if (CollectionUtil.isEmpty(annexUrl)) {
                        userNode.setCommentAttachmentsUrl(Collections.emptyList());
                    }
                }
                userNode.setNodeTime(endTime);
            } else {
                //当前任务未完成
                List<NodeCandidateInfoVO> candidateInfoVOList = candidateTypeMap.getOrDefault(CandidateTypeEnum.AUDIT.getValue(), Collections.emptyList());
                userNode.setNodeCandidateInfo(candidateInfoVOList);
                userNode.setNodeType(2);
                userNode.setAuditStatus(3);
                userNode.setNodeName(historicTaskInstance.getName());
                for (NodeCandidateInfoVO nodeCandidateInfoVO : candidateInfoVOList) {
                    userIds.add(nodeCandidateInfoVO.getUserId());
                }
            }
            if (historicTaskInstance.getName().equals("重新提交")) {
                userNode.setNodeType(7);
            }
            resultNode.add(userNode);
        }
        //封装抄送节点
        if (CollectionUtil.isNotEmpty(carbonCopyUsers)) {
            AuditNodeRecordVO carbonCopyNode = new AuditNodeRecordVO();
            carbonCopyNode.setNodeType(6);
            List<NodeCandidateInfoVO> uniqueList = new ArrayList<>(carbonCopyUsers.stream().collect(Collectors.toMap(NodeCandidateInfoVO::getUserId, user -> user, (existing, replacement) -> existing)).values());
            carbonCopyNode.setNodeCandidateInfo(uniqueList);
            resultNode.add(carbonCopyNode);
        }
        if (CollectionUtil.isNotEmpty(userIds)) {
            List<SysUser> list = sysUserService.list(new LambdaQueryWrapper<SysUser>()
                    .in(SysUser::getUserId, userIds));
            if (CollectionUtil.isNotEmpty(list)) {
                Map<Long, SysUser> userMap = list.stream().collect(Collectors.toMap(SysUser::getUserId, Function.identity()));
                for (AuditNodeRecordVO auditNodeRecordVO : resultNode) {
                    if (auditNodeRecordVO.getNodeCandidateInfo() != null) {
                        for (NodeCandidateInfoVO nodeCandidateInfoVO : auditNodeRecordVO.getNodeCandidateInfo()) {
                            SysUser user = userMap.get(nodeCandidateInfoVO.getUserId());
                            if (Objects.nonNull(user)) {
                                nodeCandidateInfoVO.setUserName(user.getNickName());
                                nodeCandidateInfoVO.setUserPhoto(user.getAvatar());
                            }
                        }
                    }
                    TaskTransferInfoDto taskTransferInfoDto = auditNodeRecordVO.getTaskTransferInfoDto();
                    if (Objects.nonNull(taskTransferInfoDto)) {
                        SysUser user = userMap.get(taskTransferInfoDto.getOriginalAssignee());
                        if (Objects.nonNull(user)) {
                            taskTransferInfoDto.setOriginalAssigneeName(user.getNickName());
                        }
                        user = userMap.get(taskTransferInfoDto.getTargetAssignee());
                        if (Objects.nonNull(user)) {
                            taskTransferInfoDto.setTargetAssigneeName(user.getNickName());
                        }
                    }
                }
            }
        }
        return resultNode;
    }

    @Override
    public Map<String, Object> selectCurrentTaskCandidateUser(String instanceId) {
        //查询该实例当前正在活动的任务
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(instanceId)
                .active()
                .list();
        if (CollectionUtil.isEmpty(tasks)) {
            return Collections.emptyMap();
        }
        List<String> candidates = new LinkedList<>();
        Task task = tasks.get(0);
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
        for (IdentityLink identityLink : identityLinks) {
            if (identityLink.getType().equals("candidate")) {
                candidates.add(identityLink.getUserId());
            }
        }
        log.info("当前任务候选人：{}", candidates);
        Date createTime = tasks.get(0).getCreateTime();
        //NodeCandidateInfoVO
        //解析xml
        List<AuditCandidateDto> candidateResult = new LinkedList<>();
        for (String candidate : candidates) {
            if (StringUtils.isNotEmpty(candidate)) {
                Map<String, Object> candidateMap = XmlUtil.xmlToMap(candidate);
                Integer type = Integer.valueOf((String) candidateMap.get("type"));
                Long userId = Long.valueOf((String) candidateMap.get("userId"));
                AuditCandidateDto candidateDto = new AuditCandidateDto();
                candidateDto.setType(type);
                candidateDto.setUserId(userId);
                candidateResult.add(candidateDto);
            }
        }
        candidateResult = candidateResult.stream().filter(x -> x.getType().equals(CandidateTypeEnum.AUDIT.getValue())).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("createTime", createTime);
        result.put("candidates", candidateResult);
        return result;
    }

    @Override
    public void remind(Long id, Integer auditType) {
        AbstractAuditBizProcessor processorBean = AuditTypeEnum.getProcessorBean(auditType);
        ApprovalSubmissionRecord record = approvalSubmissionRecordService.selectByAuditNo(processorBean.getAuditNoByBizId(id));
        if (Objects.isNull(record)) {
            throw new ServiceException("未找到审批提交记录");
        }
        if (ApprovalSubmissionRecordStatusEnum.AUDIT.getCode() != record.getApprovalStatus()) {
            throw new ServiceException("流程状态不是审批中");
        }
        Set<Long> needSendTodoUserIdSet = new HashSet<>();
        String keyPrefix = "remind:" + record.getInstanceId() + ":";
        List<Task> taskList = getActiveTaskListByProcInstId(record.getInstanceId());
        Iterator<Task> iterator = taskList.iterator();
        do {
            Task task = iterator.next();
            if (!redisCache.setnx(keyPrefix + task.getId(), StringUtils.EMPTY, 1, TimeUnit.HOURS)) {
                throw new ServiceException("催办频繁，请稍后再试");
            }
            List<IdentityLink> identityLinkList = taskService.getIdentityLinksForTask(task.getId());
            if (CollectionUtils.isEmpty(identityLinkList)) {
                log.info("节点没有候选人，不催办，taskId：{}", task.getId());
                continue;
            }
            identityLinkList.stream().filter(x -> !StringUtils.isBlank(x.getUserId()))
                    .forEach(x -> {
                        Document document = XmlUtil.parseXml(x.getUserId());
                        String type = parseXmlTargetContent(document, "type");
                        if (StringUtils.isBlank(type)) {
                            log.error("候选人设置异常，未找到<type>标签，userId：{}", x);
                            return;
                        }
                        if (Integer.valueOf(type).equals(CandidateTypeEnum.CC.getValue())) {
                            return;
                        }
                        String userId = parseXmlTargetContent(document, "userId");
                        if (StringUtils.isBlank(userId)) {
                            log.error("候选人设置异常，未找到<userId>标签，userId：{}", x);
                            return;
                        }
                        needSendTodoUserIdSet.add(Long.valueOf(userId));
                    });
        } while (iterator.hasNext());
        /*User user = userService.getById(record.getAuditInitiatorId());
        if (Objects.isNull(user)) {
            throw new BaseException(BaseCode.DATA_NOT_EXIST.getCode(), "审批发起人不存在");
        }
        AuditTypeEnum.getProcessorBean(auditType).
                sendWaitApprovalMessage(AuditRecord.builder()
                        .bizKey(String.valueOf(record.getBizId()))
                        .createUser(record.getAuditInitiatorId().intValue())
                        .createTime(record.getInitiationTime()).build(), user, needSendTodoUserIdSet, auditTypeEnum);*/
    }

    @Override
    public Task selectCurrentTask(String instanceId) {
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(instanceId)
                .active()
                .list();
        if (CollectionUtil.isEmpty(tasks)) {
            return null;
        }
        return tasks.get(0);
    }

    private List<NodeCandidateInfoVO> getNodeUserByTaskIdAndCandidateType(String taskId, CandidateTypeEnum candidateTypeEnum) {
        //该节点当前活跃节点
        Set<String> candidates = new HashSet<>();
        List<HistoricIdentityLink> historicIdentityLinksForTask = historyService.getHistoricIdentityLinksForTask(taskId);
        for (HistoricIdentityLink historicIdentityLink : historicIdentityLinksForTask) {
            if (historicIdentityLink.getType().equals("candidate")) {
                if (historicIdentityLink.getUserId() != null) {
                    candidates.add(historicIdentityLink.getUserId());
                }
            }
        }
        boolean candidateTypeEnumNullFlag = Objects.isNull(candidateTypeEnum);
        List<NodeCandidateInfoVO> candidateResult = new LinkedList<>();
        for (String candidate : candidates) {
            if (StringUtils.isNotBlank(candidate)) {
                Map<String, Object> candidateMap = XmlUtil.xmlToMap(candidate);
                Integer type = Integer.valueOf((String) candidateMap.get("type"));
                Long userId = Long.valueOf((String) candidateMap.get("userId"));
                if (candidateTypeEnumNullFlag || candidateTypeEnum.getValue().equals(type)) {
                    NodeCandidateInfoVO nodeCandidateInfoVO = new NodeCandidateInfoVO();
                    nodeCandidateInfoVO.setCandidateType(type);
                    nodeCandidateInfoVO.setUserId(userId);
                    candidateResult.add(nodeCandidateInfoVO);
                }
            }
        }
        return candidateResult;
    }

    @MultiTransactional
    @Override
    public void invokeProcessResubmitAfter(Long id, AuditTypeEnum auditTypeEnum, String remark) {
        AbstractAuditBizProcessor processorBean = AuditTypeEnum.getProcessorBean(auditTypeEnum.getCode());
        ApprovalSubmissionRecord approvalSubmissionRecord = approvalSubmissionRecordService.selectByAuditNo(processorBean.getAuditNoByBizId(id));
        if (Objects.isNull(approvalSubmissionRecord)) {
            throw new ServiceException("流程提交记录不存在");
        }
        audit(FlowableAuditParam.builder()
                .id(id).auditType(auditTypeEnum.getCode())
                .auditAction(ProcessStatusEnum.COMPLETE.getCode())
                .comment(new FlowableCommentParam(remark)).build());
    }

    @Transactional(TransactionConstant.FLOWABLE)
    @Override
    public void delProcess(AuditTypeEnum auditTypeEnum, Long bizId) {
        AbstractAuditBizProcessor processorBean = AuditTypeEnum.getProcessorBean(auditTypeEnum.getCode());
        ApprovalSubmissionRecord record = approvalSubmissionRecordService.selectByAuditNo(processorBean.getAuditNoByBizId(bizId));
        if (ApprovalSubmissionRecordStatusEnum.ROLLBACK.getCode() == record.getApprovalStatus()) {
            runtimeService.deleteProcessInstance(record.getInstanceId(), auditTypeEnum.getDesc() + "删除，流程结束");
        }
    }

    @Transactional(TransactionConstant.FLOWABLE)
    @Override
    public void batchDelProcess(AuditTypeEnum auditTypeEnum, Collection<Long> bizIds) {
        AbstractAuditBizProcessor processorBean = AuditTypeEnum.getProcessorBean(auditTypeEnum.getCode());
        List<ApprovalSubmissionRecord> list = approvalSubmissionRecordService.selectListByAuditNos(processorBean.getAuditNoByBizIds(bizIds));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(x -> {
            if (ApprovalSubmissionRecordStatusEnum.ROLLBACK.getCode() == x.getApprovalStatus()) {
                runtimeService.deleteProcessInstance(x.getInstanceId(), auditTypeEnum.getDesc() + "删除，流程结束");
            }
        });
    }
}
