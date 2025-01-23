package com.oa.listener;

import com.oa.constants.FlowableConstants;
import com.oa.enums.ProcessStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.event.impl.FlowableEntityEventImpl;
import org.flowable.engine.delegate.event.impl.FlowableProcessCancelledEventImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class ProcessInstanceStatusListener implements FlowableEventListener {

    @Lazy
    @Resource
    private RuntimeService runtimeService;

    @Override
    public void onEvent(FlowableEvent event) {
        FlowableEngineEventType eventType = (FlowableEngineEventType) event.getType();
        // 监听流程实例事件
        if (event instanceof FlowableEntityEventImpl) {
            String processInstanceId = ((FlowableEntityEventImpl) event).getProcessInstanceId();
            if (FlowableEngineEventType.PROCESS_COMPLETED.equals(eventType)) {
                log.info("流程实例 {} 已完成", processInstanceId);
                String auditResult = (String) runtimeService.getVariable(processInstanceId, FlowableConstants.AUDIT_VAR_NAME);
                runtimeService.setVariable(processInstanceId, FlowableConstants.PROCESS_STATUS, auditResult);
            } else if (FlowableEngineEventType.PROCESS_CREATED.equals(eventType)) {
                log.info("流程实例 {} 已创建", processInstanceId);
                runtimeService.setVariable(processInstanceId, FlowableConstants.PROCESS_STATUS, ProcessStatusEnum.AUDIT.getCode());
            }
        } else if (event instanceof FlowableProcessCancelledEventImpl) {
            String processInstanceId = ((FlowableProcessCancelledEventImpl) event).getProcessInstanceId();
            if (FlowableEngineEventType.PROCESS_CANCELLED.name().equals(eventType.name())) {
                log.info("流程实例 {} 已撤销", processInstanceId);
                runtimeService.setVariable(processInstanceId, FlowableConstants.PROCESS_STATUS, ProcessStatusEnum.RECALL.getCode());
            }
        }

    }

    @Override
    public boolean isFailOnException() {
        // 如果监听器在处理事件时发生异常，是否终止流程引擎，默认为 true
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        //事件监听器是否应该在事务内触发
//        DEFAULT: 表示使用默认的事务行为。在大多数情况下，这意味着事件监听器将在事务内触发。
//        INSIDE: 表示事件监听器将在事务内触发。
//        OUTSIDE: 表示事件监听器将在事务之外触发。
        return "DEFAULT";
    }
}
