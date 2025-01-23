package com.oa.listener;

import com.oa.common.utils.spring.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

@Slf4j
public class ReSubmitCandidateUserListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String startUserId = SpringUtils.getBean(HistoryService.class).createHistoricProcessInstanceQuery()
                .processInstanceId(delegateTask.getProcessInstanceId()).singleResult().getStartUserId();
//        delegateTask.addCandidateUser(FlowableServiceImpl.getUserIdXmlByUserId(Long.valueOf(startUserId)));
    }
}
