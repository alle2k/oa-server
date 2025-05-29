package com.oa.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.annotation.MultiTransactional;
import com.oa.common.core.domain.entity.SysUser;
import com.oa.common.core.page.TableDataInfo;
import com.oa.common.enums.DeletedEnum;
import com.oa.common.exception.ServiceException;
import com.oa.common.utils.OrikaMapperUtils;
import com.oa.common.utils.SecurityUtils;
import com.oa.common.utils.StringUtils;
import com.oa.core.domain.ApprovalSubmissionRecord;
import com.oa.core.domain.BusinessOrder;
import com.oa.core.domain.OrderAccountAgency;
import com.oa.core.enums.AuditTypeEnum;
import com.oa.core.mapper.master.OrderAccountAgencyMapper;
import com.oa.core.model.dto.AccountAgencyQueryDto;
import com.oa.core.model.dto.ApprovalSubmissionRecordSaveDto;
import com.oa.core.model.dto.OrderAccountAgencySaveDto;
import com.oa.core.model.dto.OrderAccountAgencyUpdDtp;
import com.oa.core.model.vo.AccountAgencyDetailVo;
import com.oa.core.service.FlowableService;
import com.oa.core.service.IApprovalSubmissionRecordService;
import com.oa.core.service.IBusinessOrderService;
import com.oa.core.service.IOrderAccountAgencyService;
import com.oa.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Resource
    private ISysUserService sysUserService;

    @MultiTransactional
    @Override
    public void add(OrderAccountAgencySaveDto dto) {
        BusinessOrder businessOrder = businessOrderService.selectOneById(dto.getOrderId());
        if (dto.getAmount().compareTo(businessOrder.getFreeAmount()) > 0) {
            throw new ServiceException("代理记账费用不能超过合同订单剩余可用金额");
        }
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

        businessOrder.setUsedAmount(businessOrder.getUsedAmount().add(entity.getAmount()));
        businessOrder.setFreeAmount(businessOrder.getFreeAmount().subtract(entity.getAmount()));
        businessOrder.setUpdateUser(userId);
        businessOrder.setUpdateTime(new Date());
        businessOrderService.updateById(businessOrder);

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

    @MultiTransactional
    @Override
    public void del(List<Long> ids) {
        update(Wrappers.<OrderAccountAgency>lambdaUpdate()
                .set(OrderAccountAgency::getDeleted, DeletedEnum.DELETED.getCode())
                .in(OrderAccountAgency::getId, ids));
        flowableService.batchDelProcess(AuditTypeEnum.APPROVAL_ACCOUNT_AGENCY, ids);
        approvalSubmissionRecordService.update(new LambdaUpdateWrapper<ApprovalSubmissionRecord>()
                .in(ApprovalSubmissionRecord::getBizId, ids)
                .eq(ApprovalSubmissionRecord::getAuditType, AuditTypeEnum.APPROVAL_ACCOUNT_AGENCY.getCode())
                .set(ApprovalSubmissionRecord::getDeleted, DeletedEnum.DELETED.getCode()));
    }

    @Override
    public TableDataInfo pageQuery(AccountAgencyQueryDto dto) {
        dto.setDataPermission(SecurityUtils.getLoginUser().getDataPermissionDto());
        Page<AccountAgencyDetailVo> page = getBaseMapper().pageQuery(new Page<>(dto.getPageNum(), dto.getPageSize()), dto);
        List<AccountAgencyDetailVo> list = page.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new TableDataInfo();
        }
        Map<Long, String> userMap = sysUserService.listByIds(list.stream().map(AccountAgencyDetailVo::getCreateUser).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickName));
        boolean userMapEmptyFlag = CollectionUtils.isEmpty(userMap);
        list.forEach(x -> {
            x.setCreateUserName(StringUtils.EMPTY);
            if (!userMapEmptyFlag) {
                x.setCreateUserName(userMap.getOrDefault(x.getCreateUser(), StringUtils.EMPTY));
            }
        });
        return new TableDataInfo(list, page.getTotal());
    }

    @MultiTransactional
    @Override
    public void modify(OrderAccountAgencyUpdDtp dto) {
        BusinessOrder businessOrder = businessOrderService.selectOneById(dto.getOrderId());
        if (dto.getAmount().compareTo(businessOrder.getFreeAmount()) > 0) {
            throw new ServiceException("代理记账费用不能超过合同订单剩余可用金额");
        }
        Date date = new Date();
        Long userId = SecurityUtils.getUserId();
        OrderAccountAgency entity = selectOneById(dto.getId());
        entity.setAmount(dto.getAmount());
        entity.setServiceBeginDate(dto.getServiceBeginDate());
        entity.setServiceEndDate(dto.getServiceEndDate());
        entity.setUpdateUser(userId);
        entity.setUpdateTime(date);
        updateById(entity);

        businessOrder.setUsedAmount(businessOrder.getUsedAmount().add(entity.getAmount()));
        businessOrder.setFreeAmount(businessOrder.getFreeAmount().subtract(entity.getAmount()));
        businessOrder.setUpdateUser(userId);
        businessOrder.setUpdateTime(date);
        flowableService.invokeProcessResubmitAfter(entity.getId(), AuditTypeEnum.APPROVAL_ACCOUNT_AGENCY, StringUtils.EMPTY);
    }

    @Override
    public AccountAgencyDetailVo detail(Long id) {
        OrderAccountAgency accountAgency = selectOneById(id);
        BusinessOrder order = businessOrderService.selectOneById(accountAgency.getOrderId());
        AccountAgencyDetailVo agencyDetailVo = OrikaMapperUtils.map(accountAgency, AccountAgencyDetailVo.class);
        agencyDetailVo.setOrderAuditNo(order.getAuditNo());
        agencyDetailVo.setPaymentTime(order.getPaymentTime());
        agencyDetailVo.setCompanyName(order.getCompanyName());
        agencyDetailVo.setCompanyContactUserName(order.getCompanyContactUserName());
        agencyDetailVo.setCompanyContactUserTel(order.getCompanyContactUserTel());
        agencyDetailVo.setOrderAmount(order.getAmount());
        agencyDetailVo.setUsedAmount(order.getUsedAmount());
        agencyDetailVo.setFreeAmount(order.getFreeAmount());
        approvalSubmissionRecordService.setCreateUserRelation(agencyDetailVo);
        return agencyDetailVo;
    }
}
