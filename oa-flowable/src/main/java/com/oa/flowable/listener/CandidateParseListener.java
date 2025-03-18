package com.oa.flowable.listener;

import cn.hutool.core.util.XmlUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.oa.common.core.domain.entity.SysUser;
import com.oa.common.exception.ServiceException;
import com.oa.common.utils.spring.SpringUtils;
import com.oa.flowable.config.ExpressEvaluationCommand;
import com.oa.flowable.enums.CandidateGroupsSelectTypeEnum;
import com.oa.flowable.enums.CandidateTypeEnum;
import com.oa.flowable.enums.ExtraNotFoundDeptLeaderEnum;
import com.oa.system.domain.SysUserRole;
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
import org.w3c.dom.NodeList;

import java.util.*;

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

    public static void main(String[] args) {
        List<String> list = new LinkedList<>();
        // 财务主管审批：[<?xml version="1.0" encoding="UTF-8" standalone="no"?><xml><selectType>1</selectType><selectItem>4</selectItem><type>0</type></xml>]
        // 财务主管、总经办抄送：[<?xml version="1.0" encoding="UTF-8" standalone="no"?><xml><selectType>1</selectType><selectItem>4</selectItem><type>1</type></xml>, <?xml version="1.0" encoding="UTF-8" standalone="no"?><xml><selectType>1</selectType><selectItem>2</selectItem><type>1</type></xml>]
//        Map<String, Object> roles = new LinkedHashMap<>();
//        roles.put("selectType", CandidateGroupsSelectTypeEnum.ROLE.getValue());
//        Stream.of(ImmutableRoleEnum.ACCOUNTANT_MANAGER.getCode(), ImmutableRoleEnum.BOSS.getCode()).forEach(x -> {
//            roles.put("selectItem", x);
//            roles.put("type", CandidateTypeEnum.CC.getValue());
//            list.add(XmlUtil.mapToXmlStr(roles));
//        });
        // 部门负责人审批：[<?xml version="1.0" encoding="UTF-8" standalone="no"?><xml><selectType>2</selectType><selectItem/><type>0</type><extraNotFoundDeptLeader>0</extraNotFoundDeptLeader></xml>]
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("selectType", CandidateGroupsSelectTypeEnum.DEPT_MANAGER.getValue());
        map.put("selectItem", StringUtils.EMPTY);
        map.put("type", CandidateTypeEnum.AUDIT.getValue());
        map.put("extraNotFoundDeptLeader", ExtraNotFoundDeptLeaderEnum.RECURSIVE_UP_DEPT_LEADER.getValue());
        list.add(XmlUtil.mapToXmlStr(map));
        System.out.println(list);
    }

    private void doCandidateParse(List<SequenceFlow> sequenceFlowList, String currentUserId, VariableScope variableScope) {
        SysUser user = SpringUtils.getBean(ISysUserService.class).getById(currentUserId);
        if (Objects.isNull(user)) {
            throw new ServiceException("发起人不存在");
        }
        ManagementService managementService = SpringUtils.getBean(ManagementService.class);
        ISysUserRoleService userRoleService = SpringUtils.getBean(ISysUserRoleService.class);
        Set<Long> deptLeaderIdList = SpringUtils.getBean(ISysDeptService.class).recursiveGetDeptLeader(Collections.singleton(user.getDeptId()));
        boolean deptLeaderIdListEmptyFlag = CollectionUtils.isEmpty(deptLeaderIdList);
        Map<String, Object> paramMap = new LinkedHashMap<>();
        sequenceFlowList.forEach(x -> {
            FlowElement targetFlowElement = x.getTargetFlowElement();
            if (!UserTask.class.isAssignableFrom(targetFlowElement.getClass())) {
                log.info("下游节点不是用户任务，不解析候选信息，param：{}", targetFlowElement);
                return;
            }
            UserTask userTask = (UserTask) targetFlowElement;
            List<String> candidateGroupList = userTask.getCandidateGroups();
            if (CollectionUtils.isEmpty(candidateGroupList)) {
                log.info("节点未配置候选组，不需要解析，taskId：{}", userTask.getId());
                return;
            }
            List<Map<String, Object>> list = new LinkedList<>();
            paramMap.put(userTask.getId(), list);
            String skipExpression = userTask.getSkipExpression();
            boolean skipExpressionBlankFlag = StringUtils.isBlank(skipExpression);
            if (!skipExpressionBlankFlag) {
                skipExpression = skipExpression.substring("${".length(), skipExpression.indexOf("}"));
            }
            String conditionExpression = x.getConditionExpression();
            if (StringUtils.isNotBlank(conditionExpression)) {
                if (!managementService.executeCommand(new ExpressEvaluationCommand(conditionExpression, variableScope))) {
                    log.info("表达式结果为false，不走该分支，不进行候选组解析，节点ID：{}", userTask.getId());
                    return;
                }
            }
            Iterator<String> iterator = candidateGroupList.iterator();
            do {
                String group = iterator.next();
                Document document = XmlUtil.parseXml(group);
                NodeList nodeList = document.getElementsByTagName("selectType");
                if (Objects.isNull(nodeList)) {
                    log.error("候选组设置异常，未找到<selectType>标签，group：{}", group);
                    continue;
                }
                CandidateGroupsSelectTypeEnum selectTypeEnum = CandidateGroupsSelectTypeEnum.codeMap.get(
                        Integer.valueOf(nodeList.item(0).getTextContent()));
                nodeList = document.getElementsByTagName("selectItem");
                if (Objects.isNull(nodeList)) {
                    log.error("候选组设置异常，未找到<selectItem>标签，group：{}", group);
                    continue;
                }
                String selectItem = nodeList.item(0).getTextContent();
                nodeList = document.getElementsByTagName("type");
                if (Objects.isNull(nodeList)) {
                    log.error("候选组设置异常，未找到<type>标签，group：{}", group);
                    continue;
                }
                String type = nodeList.item(0).getTextContent();
                switch (selectTypeEnum) {
                    case DEPT_SUPERVISOR:
                    case DEPT_MANAGER:
                        if (deptLeaderIdListEmptyFlag) {
                            nodeList = document.getElementsByTagName("extraNotFoundDeptLeader");
                            ExtraNotFoundDeptLeaderEnum notFoundEnum = ExtraNotFoundDeptLeaderEnum.codeMap.get(
                                    Integer.valueOf(nodeList.item(0).getTextContent()));
                            switch (notFoundEnum) {
                                case RECURSIVE_UP_DEPT_LEADER:
                                    /*if (CollectionUtils.isEmpty(upDeptLeaderIdList)) {
                                        log.info("未找到上级部门领导，部门ID列表：{}", deptIdList);
                                        break;
                                    }
                                    upDeptLeaderIdList.forEach(userId -> {
                                        Map<String, Object> map = new LinkedHashMap<>();
                                        map.put("userId", userId);
                                        map.put("type", type);
                                        list.add(map);
                                    });*/
                                    break;
                                case PASS:
                                    if (skipExpressionBlankFlag) {
                                        break;
                                    }
                                    variableScope.setTransientVariable(skipExpression, true);
                                    variableScope.setTransientVariable("_FLOWABLE_SKIP_EXPRESSION_ENABLED", true);
                                    break;
                                case ADMIN_AUDIT:
                                    /*if (CollectionUtils.isEmpty(adminList)) {
                                        log.info("系统超管角色无用户，userId：{}", adminList);
                                        break;
                                    }
                                    adminList.forEach(userId -> {
                                        Map<String, Object> map = new LinkedHashMap<>();
                                        map.put("userId", userId);
                                        map.put("type", type);
                                        list.add(map);
                                    });*/
                            }
                            break;
                        }
                        deptLeaderIdList.forEach(userId -> {
                            Map<String, Object> map = new LinkedHashMap<>();
                            map.put("userId", userId);
                            map.put("type", type);
                            list.add(map);
                        });
                        break;
                    case ROLE:
                        List<SysUserRole> userRoleList = userRoleService.selectListByRoleIds(Collections.singleton(Long.valueOf(selectItem)));
                        if (CollectionUtils.isEmpty(userRoleList)) {
                            /*nodeList = document.getElementsByTagName("extraNotFoundRole");
                            if (Objects.isNull(nodeList)) {
                                log.error("角色为空，且未配置<extraNotFoundRole>，group：{}", group);
                                break;
                            }
                            ExtraNotFoundRoleEnum notFoundEnum = ExtraNotFoundRoleEnum.getEnumByValue(
                                    Integer.valueOf(nodeList.item(0).getTextContent()));
                            if (notFoundEnum == ExtraNotFoundRoleEnum.PASS) {
                                if (skipExpressionBlankFlag) {
                                    break;
                                }
                                variableScope.setTransientVariable(skipExpression, true);
                                variableScope.setTransientVariable("_FLOWABLE_SKIP_EXPRESSION_ENABLED", true);
                            } else {
                                adminList.forEach(userId -> {
                                    Map<String, Object> map = new LinkedHashMap<>();
                                    map.put("userId", userId);
                                    map.put("type", type);
                                    list.add(map);
                                });
                            }*/
                            break;
                        }
                        userRoleList.forEach(userRole -> {
                            Map<String, Object> map = new LinkedHashMap<>();
                            map.put("userId", userRole.getUserId());
                            map.put("type", type);
                            list.add(map);
                        });
                }
            } while (iterator.hasNext());
        });
        if (CollectionUtils.isEmpty(paramMap)) {
            return;
        }
        variableScope.setTransientVariables(paramMap);
    }
}
