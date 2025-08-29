package com.oa.core.processor;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.oa.common.utils.OrikaMapperUtils;
import com.oa.core.domain.BusinessOrder;
import com.oa.core.domain.BusinessOrderRef;
import com.oa.core.domain.OrderAccountAgency;
import com.oa.core.domain.OrderAccountAgencyAccount;
import com.oa.core.enums.ApprovalSubmissionRecordStatusEnum;
import com.oa.core.enums.BusinessOrderItemBizTypeEnum;
import com.oa.core.service.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrderAccountAgencyAuditProcessor extends AbstractAuditBizProcessor {

    @Resource
    private IOrderAccountAgencyService orderAccountAgencyService;
    @Resource
    private IOrderAccountAgencyAccountService orderAccountAgencyAccountService;
    @Resource
    private IBusinessOrderService businessOrderService;
    @Resource
    private IBusinessOrderItemService businessOrderItemService;
    @Resource
    private IBusinessOrderRefService businessOrderRefService;

    @Override
    public String getAuditNoByBizId(Long bizId) {
        return orderAccountAgencyService.getById(bizId).getAuditNo();
    }

    @Override
    public List<String> getAuditNoByBizIds(Collection<Long> bizIds) {
        return orderAccountAgencyService.listByIds(bizIds).stream().map(OrderAccountAgency::getAuditNo).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void whenPass(Long bizId) {
        OrderAccountAgency entity = orderAccountAgencyService.selectOneById(bizId);
        BusinessOrder businessOrder = businessOrderService.selectOneById(entity.getOrderId());
        Date now = new Date();
        orderAccountAgencyService.update(Wrappers.<OrderAccountAgency>lambdaUpdate()
                .set(OrderAccountAgency::getApprovalTime, now)
                .set(OrderAccountAgency::getApprovalStatus, ApprovalSubmissionRecordStatusEnum.PASS.getCode())
                .eq(OrderAccountAgency::getId, bizId));
        Long orderId = entity.getOrderId();
        if (businessOrderItemService.selectListByOrderIds(Collections.singleton(entity.getOrderId()))
                .stream().anyMatch(x -> BusinessOrderItemBizTypeEnum.ACCOUNT_AGENCY_RENEW.getCode() == x.getBizType())) {
            orderId = businessOrderRefService.selectListByOrderIds(Collections.singleton(entity.getOrderId())).get(0).getRefId();
        }
        OrderAccountAgencyAccount account = orderAccountAgencyAccountService.selectOneByOrderId(orderId);
        if (Objects.isNull(account)) {
            account = OrikaMapperUtils.map(entity, OrderAccountAgencyAccount.class);
            account.setId(null);
            account.setContractNo(businessOrder.getAuditNo());
            account.setCreateTime(now);
            account.setUpdateTime(now);
            populate(account, businessOrder);
            orderAccountAgencyAccountService.save(account);
            return;
        }
        account.setAmount(entity.getAmount());
        account.setServiceBeginDate(entity.getServiceBeginDate());
        account.setServiceEndDate(entity.getServiceEndDate());
        account.setUpdateTime(now);
        populate(account, businessOrder);
        orderAccountAgencyAccountService.updateById(account);
    }

    private void populate(OrderAccountAgencyAccount account, BusinessOrder businessOrder) {
        account.setOrderAmount(businessOrder.getAmount());
        account.setPaymentTime(businessOrder.getPaymentTime());
        account.setCompanyName(businessOrder.getCompanyName());
        account.setCompanyContactUserName(businessOrder.getCompanyContactUserName());
        account.setCompanyContactUserTel(businessOrder.getCompanyContactUserTel());
        account.setAnnexUrl(businessOrder.getAnnexUrl());
        account.setPaymentScreenshot(businessOrder.getPaymentScreenshot());
    }

    @Override
    public void whenReject(Long bizId) {
        releaseOrderAmount(orderAccountAgencyService.selectOneById(bizId));
        orderAccountAgencyService.update(Wrappers.<OrderAccountAgency>lambdaUpdate()
                .set(OrderAccountAgency::getApprovalTime, new Date())
                .set(OrderAccountAgency::getApprovalStatus, ApprovalSubmissionRecordStatusEnum.REJECT.getCode())
                .eq(OrderAccountAgency::getId, bizId));
    }

    private void releaseOrderAmount(OrderAccountAgency entity) {
        BusinessOrder businessOrder = businessOrderService.selectOneById(entity.getOrderId());
        businessOrder.setUsedAmount(businessOrder.getUsedAmount().subtract(entity.getAmount()));
        businessOrder.setFreeAmount(businessOrder.getFreeAmount().add(entity.getAmount()));
        businessOrder.setUpdateTime(new Date());
        businessOrderService.updateById(businessOrder);
    }

    @Override
    public void whenRevoke(Long bizId, ApprovalSubmissionRecordStatusEnum statusEnum) {
        releaseOrderAmount(orderAccountAgencyService.selectOneById(bizId));
        orderAccountAgencyService.update(Wrappers.<OrderAccountAgency>lambdaUpdate()
                .set(OrderAccountAgency::getApprovalStatus, statusEnum.getCode())
                .eq(OrderAccountAgency::getId, bizId));
    }
}
