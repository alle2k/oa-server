package com.oa.core.processor;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.oa.core.domain.BusinessOrder;
import com.oa.core.enums.ApprovalSubmissionRecordStatusEnum;
import com.oa.core.service.IBusinessOrderService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
public class BusinessOrderAuditProcessor extends AbstractAuditBizProcessor {

    @Resource
    private IBusinessOrderService businessOrderService;

    @Override
    public String getAuditNoByBizId(Long bizId) {
        return businessOrderService.getById(bizId).getAuditNo();
    }

    @Override
    public void whenPass(Long bizId) {
        businessOrderService.update(Wrappers.<BusinessOrder>lambdaUpdate()
                .set(BusinessOrder::getApprovalTime, new Date())
                .set(BusinessOrder::getApprovalStatus, ApprovalSubmissionRecordStatusEnum.PASS.getCode())
                .eq(BusinessOrder::getId, bizId));
    }

    @Override
    public void whenReject(Long bizId) {
        businessOrderService.update(Wrappers.<BusinessOrder>lambdaUpdate()
                .set(BusinessOrder::getApprovalTime, new Date())
                .set(BusinessOrder::getApprovalStatus, ApprovalSubmissionRecordStatusEnum.REJECT.getCode())
                .eq(BusinessOrder::getId, bizId));
    }

    @Override
    public void whenRevoke(Long bizId, ApprovalSubmissionRecordStatusEnum statusEnum) {
        businessOrderService.update(Wrappers.<BusinessOrder>lambdaUpdate()
                .set(BusinessOrder::getApprovalStatus, statusEnum.getCode())
                .eq(BusinessOrder::getId, bizId));
    }
}
