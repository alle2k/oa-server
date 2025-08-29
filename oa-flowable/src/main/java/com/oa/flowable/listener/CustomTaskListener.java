package com.oa.flowable.listener;

import com.oa.common.utils.spring.SpringUtils;
import com.oa.flowable.constants.FlowableConstants;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

@Slf4j
public class CustomTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        RuntimeService runtimeService = SpringUtils.getBean("runtimeService");
        TaskService taskService = SpringUtils.getBean("taskService");
        // 在此处编写任务执行事件的逻辑
        log.info("用户任务开始执行，任务名称：{}，执行人：{}", delegateTask.getName(), delegateTask.getAssignee());
        String auditResult = (String) runtimeService.getVariable(delegateTask.getProcessInstanceId(), FlowableConstants.AUDIT_VAR_NAME);
        // 其他任务执行逻辑
        log.info("用户任务执行结束，任务名称：{}，执行结果：{}", delegateTask.getName(), auditResult);
        taskService.setVariableLocal(delegateTask.getId(), FlowableConstants.NODE_VAR_NAME, auditResult);
    }
}
