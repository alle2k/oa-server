package com.oa.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.annotation.MultiTransactional;
import com.oa.common.core.domain.entity.SysUser;
import com.oa.common.utils.OrikaMapperUtils;
import com.oa.common.utils.SecurityUtils;
import com.oa.common.utils.StringUtils;
import com.oa.core.domain.ApprovalSubmissionRecord;
import com.oa.core.domain.BusinessOrder;
import com.oa.core.enums.AuditTypeEnum;
import com.oa.core.enums.DeletedEnum;
import com.oa.core.mapper.master.BusinessOrderMapper;
import com.oa.core.model.dto.ApprovalSubmissionRecordSaveDto;
import com.oa.core.model.dto.BusinessOrderQueryDto;
import com.oa.core.model.dto.BusinessOrderSaveDto;
import com.oa.core.model.dto.BusinessOrderUpdDto;
import com.oa.core.model.vo.BusinessOrderShortVo;
import com.oa.core.service.FlowableService;
import com.oa.core.service.IApprovalSubmissionRecordService;
import com.oa.core.service.IBusinessOrderService;
import com.oa.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BusinessOrderServiceImpl extends ServiceImpl<BusinessOrderMapper, BusinessOrder> implements IBusinessOrderService {

    @Resource
    private ISysUserService sysUserService;
    @Resource
    private IApprovalSubmissionRecordService approvalSubmissionRecordService;
    @Resource
    private FlowableService flowableService;

    @MultiTransactional
    @Override
    public void add(BusinessOrderSaveDto saveDto) {
        Long userId = SecurityUtils.getUserId();
        BusinessOrder entity = OrikaMapperUtils.map(saveDto, BusinessOrder.class);
        List<String> annexUrlList = saveDto.getAnnexUrlList();
        if (!CollectionUtils.isEmpty(annexUrlList)) {
            entity.setAnnexUrl(String.join(",", annexUrlList));
        }
        List<String> paymentScreenshotList = saveDto.getPaymentScreenshotList();
        if (!CollectionUtils.isEmpty(paymentScreenshotList)) {
            entity.setPaymentScreenshot(String.join(",", paymentScreenshotList));
        }
        entity.setCreateUser(userId);
        entity.setUpdateUser(userId);
        save(entity);

        String instanceId = flowableService.startProcess(entity.getId(), AuditTypeEnum.APPROVAL_BUSINESS_ORDER);
        String auditNo = approvalSubmissionRecordService.add(ApprovalSubmissionRecordSaveDto.builder()
                .auditTypeEnum(AuditTypeEnum.APPROVAL_BUSINESS_ORDER)
                .instanceId(instanceId)
                .bizId(entity.getId())
                .remark(entity.getRemark())
                .build());
        entity.setAuditNo(auditNo);
        updateById(entity);
    }

    @Override
    public List<BusinessOrderShortVo> pageQuery(BusinessOrderQueryDto queryDto) {
        Page<BusinessOrder> page = getBaseMapper().pageQuery(new Page<>(queryDto.getPageNum(), queryDto.getPageSize()), queryDto);
        List<BusinessOrder> list = page.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, String> userMap = sysUserService.listByIds(list.stream().map(BusinessOrder::getCreateUser).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickName));
        boolean userMapEmptyFlag = CollectionUtils.isEmpty(userMap);
        return list.stream().map(x -> {
            BusinessOrderShortVo resultVo = OrikaMapperUtils.map(x, BusinessOrderShortVo.class);
            if (!userMapEmptyFlag) {
                resultVo.setCreateUserName(userMap.getOrDefault(x.getCreateUser(), StringUtils.EMPTY));
            }
            resultVo.setAnnexUrlList(Collections.emptyList());
            resultVo.setPaymentScreenshotList(Collections.emptyList());
            if (!StringUtils.isBlank(x.getAnnexUrl())) {
                resultVo.setAnnexUrlList(Arrays.stream(x.getAnnexUrl().split(",")).collect(Collectors.toList()));
            }
            if (!StringUtils.isBlank(x.getPaymentScreenshot())) {
                resultVo.setPaymentScreenshotList(Arrays.stream(x.getPaymentScreenshot().split(",")).collect(Collectors.toList()));
            }
            return resultVo;
        }).collect(Collectors.toList());
    }

    @MultiTransactional
    @Override
    public void modify(BusinessOrderUpdDto updDto) {
        BusinessOrder entity = selectOneById(updDto.getId());
        entity.setPaymentTime(updDto.getPaymentTime());
        entity.setCompanyName(updDto.getCompanyName().trim());
        entity.setCompanyContactUserName(updDto.getCompanyContactUserName().trim());
        entity.setCompanyContactUserTel(updDto.getCompanyContactUserTel().trim());
        entity.setAmount(updDto.getAmount());
        entity.setRemark(updDto.getRemark().trim());
        entity.setAnnexUrl(StringUtils.EMPTY);
        if (!CollectionUtils.isEmpty(updDto.getAnnexUrlList())) {
            entity.setAnnexUrl(String.join(",", updDto.getAnnexUrlList()));
        }
        entity.setPaymentScreenshot(StringUtils.EMPTY);
        if (!CollectionUtils.isEmpty(updDto.getPaymentScreenshotList())) {
            entity.setPaymentScreenshot(String.join(",", updDto.getPaymentScreenshotList()));
        }
        entity.setUpdateUser(SecurityUtils.getUserId());
        entity.setUpdateTime(new Date());
        updateById(entity);
        flowableService.invokeProcessResubmitAfter(entity.getId(), AuditTypeEnum.APPROVAL_BUSINESS_ORDER, updDto.getRemark());
    }

    @MultiTransactional
    @Override
    public void del(List<Long> ids) {
        update(Wrappers.<BusinessOrder>lambdaUpdate()
                .set(BusinessOrder::getDeleted, DeletedEnum.DELETED.getCode())
                .in(BusinessOrder::getId, ids));
        flowableService.batchDelProcess(AuditTypeEnum.APPROVAL_BUSINESS_ORDER, ids);
        approvalSubmissionRecordService.update(new LambdaUpdateWrapper<ApprovalSubmissionRecord>()
                .in(ApprovalSubmissionRecord::getBizId, ids)
                .eq(ApprovalSubmissionRecord::getAuditType, AuditTypeEnum.APPROVAL_BUSINESS_ORDER.getCode())
                .set(ApprovalSubmissionRecord::getDeleted, DeletedEnum.DELETED.getCode()));
    }
}
