package com.oa.flowable.listener;

import cn.hutool.core.util.XmlUtil;
import com.oa.common.utils.spring.SpringUtils;
import com.oa.flowable.enums.CandidateTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class ReSubmitCandidateUserListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String startUserId = SpringUtils.getBean(HistoryService.class).createHistoricProcessInstanceQuery()
                .processInstanceId(delegateTask.getProcessInstanceId()).singleResult().getStartUserId();
        delegateTask.addCandidateUser(getUserIdXmlByUserId(Long.valueOf(startUserId)));
    }

    private String getUserIdXmlByUserId(Long userId) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", userId);
        map.put("type", CandidateTypeEnum.AUDIT.getValue());
        return XmlUtil.mapToXmlStr(map);
    }
}
