package com.oa.flowable.listener;

import cn.hutool.core.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class PopulateListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        List<?> userInfoList = (List<?>) delegateTask.getTransientVariable(delegateTask.getTaskDefinitionKey());
        if (CollectionUtils.isEmpty(userInfoList)) {
            return;
        }
        delegateTask.addCandidateUsers(userInfoList.stream().map(x -> XmlUtil.mapToXmlStr((Map<?, ?>) x)).collect(Collectors.toList()));
    }
}
