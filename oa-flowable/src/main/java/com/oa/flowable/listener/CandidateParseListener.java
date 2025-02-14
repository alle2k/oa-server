package com.oa.flowable.listener;

import cn.hutool.core.util.XmlUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.oa.common.core.domain.entity.SysUser;
import com.oa.common.utils.spring.SpringUtils;
import com.oa.flowable.config.ExpressEvaluationCommand;
import com.oa.flowable.enums.CandidateTypeEnum;
import com.oa.system.domain.SysUserRole;
import com.oa.system.enums.ImmutableRoleEnum;
import com.oa.system.service.ISysDeptService;
import com.oa.system.service.ISysUserRoleService;
import com.oa.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.variable.api.delegate.VariableScope;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CandidateParseListener implements TaskListener, ExecutionListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        RepositoryService repositoryService = SpringUtils.getBean(RepositoryService.class);
        BpmnModel bpmnModel = repositoryService.getBpmnModel(delegateTask.getProcessDefinitionId());
        FlowElement flowElement = bpmnModel.getFlowElement(delegateTask.getTaskDefinitionKey());
        if (!UserTask.class.isAssignableFrom(flowElement.getClass())) {
            log.info("当前节点不是用户任务类型，不解析候选信息，flowElement：{}", flowElement);
            return;
        }
        String assigneeXml = delegateTask.getAssignee();
        Document document = XmlUtil.parseXml(assigneeXml);
        doCandidateParse(((UserTask) flowElement).getOutgoingFlows(), document.getElementsByTagName("userId").item(0).getTextContent(), delegateTask);
    }

    @Override
    public void notify(DelegateExecution delegateExecution) {
        FlowNode flowNode = (FlowNode) delegateExecution.getCurrentFlowElement();
        List<SequenceFlow> sequenceFlowList = flowNode.getOutgoingFlows();
        if (CollectionUtils.isEmpty(sequenceFlowList)) {
            log.info("未找到下游节点，param：{}", delegateExecution);
            return;
        }
        HistoryService historyService = SpringUtils.getBean(HistoryService.class);
        String startUserId = historyService.createHistoricProcessInstanceQuery().processInstanceId(delegateExecution.getProcessInstanceId()).unfinished()
                .singleResult().getStartUserId();
        doCandidateParse(sequenceFlowList, startUserId, delegateExecution);

        Long bizId = delegateExecution.getVariable("bizId", Long.class);
        Integer bizType = delegateExecution.getVariable("bizType", Integer.class);
        delegateExecution.removeVariable("bizId");
        delegateExecution.removeVariable("bizType");
        delegateExecution.setTransientVariable("bizId", bizId);
        delegateExecution.setTransientVariable("bizType", bizType);
    }

    private void doCandidateParse(List<SequenceFlow> sequenceFlowList, String currentUserId, VariableScope variableScope) {
        ManagementService managementService = SpringUtils.getBean(ManagementService.class);
        SysUser user = SpringUtils.getBean(ISysUserService.class).getById(currentUserId);
        Set<Long> auditUserIds = SpringUtils.getBean(ISysDeptService.class).recursiveGetDeptLeader(Collections.singleton(user.getDeptId()));
        Set<Long> ccUserIds = SpringUtils.getBean(ISysUserRoleService.class).selectListByRoleIds(
                        Stream.of(ImmutableRoleEnum.ACCOUNTANT.getCode(), ImmutableRoleEnum.BOSS.getCode()).map(Long::valueOf).collect(Collectors.toList()))
                .stream().map(SysUserRole::getUserId).collect(Collectors.toSet());
        boolean auditUserIdsEmptyFlag = CollectionUtils.isEmpty(auditUserIds);
        boolean ccUserIdsEmptyFlag = CollectionUtils.isEmpty(ccUserIds);
        if (auditUserIdsEmptyFlag && ccUserIdsEmptyFlag) {
            return;
        }
        Map<String, Object> paramMap = new LinkedHashMap<>();
        sequenceFlowList.forEach(x -> {
            FlowElement targetFlowElement = x.getTargetFlowElement();
            if (!UserTask.class.isAssignableFrom(targetFlowElement.getClass())) {
                log.info("下游节点不是用户任务，不解析候选信息，param：{}", targetFlowElement);
                return;
            }
            UserTask userTask = (UserTask) targetFlowElement;
            List<Map<String, Object>> list = new LinkedList<>();
            paramMap.put(userTask.getId(), list);
            String conditionExpression = x.getConditionExpression();
            if (StringUtils.isNotBlank(conditionExpression)) {
                if (!managementService.executeCommand(new ExpressEvaluationCommand(conditionExpression, variableScope))) {
                    log.info("表达式结果为false，不走该分支，不进行候选组解析，节点ID：{}", userTask.getId());
                    return;
                }
            }
            if (!auditUserIdsEmptyFlag) {
                auditUserIds.forEach(userId -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("userId", userId);
                    map.put("type", CandidateTypeEnum.AUDIT.getValue());
                    list.add(map);
                });
            }
            if (!ccUserIdsEmptyFlag) {
                ccUserIds.forEach(userId -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("userId", userId);
                    map.put("type", CandidateTypeEnum.CC.getValue());
                    list.add(map);
                });
            }
        });
        variableScope.setTransientVariables(paramMap);
    }
}
