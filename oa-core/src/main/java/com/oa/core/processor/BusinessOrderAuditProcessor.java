package com.oa.core.processor;

import com.oa.core.enums.ApprovalSubmissionRecordStatusEnum;
import com.oa.core.service.IBusinessOrderService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class BusinessOrderAuditProcessor extends AbstractAuditBizProcessor {

    @Resource
    private IBusinessOrderService businessOrderService;

    @Override
    public String getAuditNoByBizId(Long bizId) {
        return businessOrderService.getById(bizId).getAuditNo();
    }

    @Override
    public void whenRevoke(Long bizId, ApprovalSubmissionRecordStatusEnum statusEnum) {

    }
}
