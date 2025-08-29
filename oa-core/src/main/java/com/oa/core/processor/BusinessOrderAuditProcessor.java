package com.oa.core.processor;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.oa.common.exception.ServiceException;
import com.oa.core.domain.BusinessOrder;
import com.oa.core.enums.ApprovalSubmissionRecordStatusEnum;
import com.oa.core.model.dto.AuditFormBusinessOrderDto;
import com.oa.core.service.IBusinessOrderService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BusinessOrderAuditProcessor extends AbstractAuditBizProcessor {

    @Resource
    private IBusinessOrderService businessOrderService;

    @Override
    public String getAuditNoByBizId(Long bizId) {
        return businessOrderService.getById(bizId).getAuditNo();
    }

    @Override
    public List<String> getAuditNoByBizIds(Collection<Long> bizIds) {
        return businessOrderService.listByIds(bizIds).stream().map(BusinessOrder::getAuditNo).collect(Collectors.toList());
    }

    @Override
    public void whenPass(Long bizId) {
        BusinessOrder entity = businessOrderService.selectOneById(bizId);
        if (entity.getPerformance().compareTo(entity.getAmount()) > 0) {
            throw new ServiceException("业绩不能超过合同金额");
        }
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

    @Override
    public void invoke(Long bizId, Object obj) {
        AuditFormBusinessOrderDto formDto = (AuditFormBusinessOrderDto) obj;
        businessOrderService.update(Wrappers.<BusinessOrder>lambdaUpdate()
                .set(BusinessOrder::getPerformance, formDto.getPerformance())
                .eq(BusinessOrder::getId, bizId));
    }
}
