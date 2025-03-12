package com.oa.core.processor;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.oa.core.domain.OrderAccountAgency;
import com.oa.core.enums.ApprovalSubmissionRecordStatusEnum;
import com.oa.core.service.IOrderAccountAgencyService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderAccountAgencyAuditProcessor extends AbstractAuditBizProcessor {

    @Resource
    private IOrderAccountAgencyService orderAccountAgencyService;

    @Override
    public String getAuditNoByBizId(Long bizId) {
        return orderAccountAgencyService.getById(bizId).getAuditNo();
    }

    @Override
    public List<String> getAuditNoByBizIds(Collection<Long> bizIds) {
        return orderAccountAgencyService.listByIds(bizIds).stream().map(OrderAccountAgency::getAuditNo).collect(Collectors.toList());
    }

    @Override
    public void whenPass(Long bizId) {
        orderAccountAgencyService.update(Wrappers.<OrderAccountAgency>lambdaUpdate()
                .set(OrderAccountAgency::getApprovalTime, new Date())
                .set(OrderAccountAgency::getApprovalStatus, ApprovalSubmissionRecordStatusEnum.PASS.getCode())
                .eq(OrderAccountAgency::getId, bizId));
    }

    @Override
    public void whenReject(Long bizId) {
        orderAccountAgencyService.update(Wrappers.<OrderAccountAgency>lambdaUpdate()
                .set(OrderAccountAgency::getApprovalTime, new Date())
                .set(OrderAccountAgency::getApprovalStatus, ApprovalSubmissionRecordStatusEnum.REJECT.getCode())
                .eq(OrderAccountAgency::getId, bizId));
    }

    @Override
    public void whenRevoke(Long bizId, ApprovalSubmissionRecordStatusEnum statusEnum) {
        orderAccountAgencyService.update(Wrappers.<OrderAccountAgency>lambdaUpdate()
                .set(OrderAccountAgency::getApprovalStatus, statusEnum.getCode())
                .eq(OrderAccountAgency::getId, bizId));
    }
}
