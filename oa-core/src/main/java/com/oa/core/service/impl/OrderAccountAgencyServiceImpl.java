package com.oa.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.annotation.MultiTransactional;
import com.oa.common.utils.OrikaMapperUtils;
import com.oa.common.utils.SecurityUtils;
import com.oa.core.domain.ApprovalSubmissionRecord;
import com.oa.core.domain.BusinessOrder;
import com.oa.core.domain.OrderAccountAgency;
import com.oa.core.enums.AuditTypeEnum;
import com.oa.core.enums.DeletedEnum;
import com.oa.core.mapper.master.OrderAccountAgencyMapper;
import com.oa.core.model.dto.ApprovalSubmissionRecordSaveDto;
import com.oa.core.model.dto.OrderAccountAgencySaveDto;
import com.oa.core.service.FlowableService;
import com.oa.core.service.IApprovalSubmissionRecordService;
import com.oa.core.service.IBusinessOrderService;
import com.oa.core.service.IOrderAccountAgencyService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @auther CodeGenerator
 * @create 2025-03-12 19:05:13
 * @describe 代理记账服务实现类
 */
@Service
public class OrderAccountAgencyServiceImpl extends ServiceImpl<OrderAccountAgencyMapper, OrderAccountAgency> implements IOrderAccountAgencyService {

    @Resource
    private IBusinessOrderService businessOrderService;
    @Resource
    private FlowableService flowableService;
    @Resource
    private IApprovalSubmissionRecordService approvalSubmissionRecordService;

    @MultiTransactional
    @Override
    public void add(OrderAccountAgencySaveDto dto) {
        BusinessOrder businessOrder = businessOrderService.selectOneById(dto.getOrderId());
        Long userId = SecurityUtils.getUserId();
        OrderAccountAgency entity = OrikaMapperUtils.map(dto, OrderAccountAgency.class);
        List<String> annexUrlList = dto.getAnnexUrlList();
        if (!CollectionUtils.isEmpty(annexUrlList)) {
            entity.setAnnexUrl(String.join(",", annexUrlList));
        }
        entity.setOrderId(businessOrder.getId());
        entity.setCreateUser(userId);
        entity.setUpdateUser(userId);
        save(entity);

        String instanceId = flowableService.startProcess(entity.getId(), AuditTypeEnum.APPROVAL_ACCOUNT_AGENCY);
        String auditNo = approvalSubmissionRecordService.add(ApprovalSubmissionRecordSaveDto.builder()
                .auditTypeEnum(AuditTypeEnum.APPROVAL_ACCOUNT_AGENCY)
                .instanceId(instanceId)
                .bizId(entity.getId())
                .remark(entity.getRemark())
                .build());
        entity.setAuditNo(auditNo);
        updateById(entity);
    }

    @MultiTransactional
    @Override
    public void del(Long id) {
        update(Wrappers.<OrderAccountAgency>lambdaUpdate()
                .set(OrderAccountAgency::getDeleted, DeletedEnum.DELETED.getCode())
                .eq(OrderAccountAgency::getId, id));
        flowableService.delProcess(AuditTypeEnum.APPROVAL_ACCOUNT_AGENCY, id);
        approvalSubmissionRecordService.update(new LambdaUpdateWrapper<ApprovalSubmissionRecord>()
                .eq(ApprovalSubmissionRecord::getBizId, id)
                .eq(ApprovalSubmissionRecord::getAuditType, AuditTypeEnum.APPROVAL_ACCOUNT_AGENCY.getCode())
                .set(ApprovalSubmissionRecord::getDeleted, DeletedEnum.DELETED.getCode()));
    }
}
