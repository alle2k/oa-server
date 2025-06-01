package com.oa.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.annotation.MultiTransactional;
import com.oa.common.core.domain.entity.SysUser;
import com.oa.common.core.page.TableDataInfo;
import com.oa.common.enums.DeletedEnum;
import com.oa.common.error.BaseCode;
import com.oa.common.exception.ServiceException;
import com.oa.common.utils.OrikaMapperUtils;
import com.oa.common.utils.SecurityUtils;
import com.oa.common.utils.StringUtils;
import com.oa.core.domain.ApprovalSubmissionRecord;
import com.oa.core.domain.BusinessOrder;
import com.oa.core.domain.BusinessOrderItem;
import com.oa.core.domain.BusinessOrderRef;
import com.oa.core.enums.ApprovalSubmissionRecordStatusEnum;
import com.oa.core.enums.AuditTypeEnum;
import com.oa.core.enums.BusinessOrderItemBizTypeEnum;
import com.oa.core.mapper.master.BusinessOrderMapper;
import com.oa.core.model.dto.ApprovalSubmissionRecordSaveDto;
import com.oa.core.model.dto.BusinessOrderQueryDto;
import com.oa.core.model.dto.BusinessOrderSaveDto;
import com.oa.core.model.dto.BusinessOrderUpdDto;
import com.oa.core.model.vo.BusinessOrderDetailVo;
import com.oa.core.model.vo.BusinessOrderItemDetailVo;
import com.oa.core.model.vo.BusinessOrderShortVo;
import com.oa.core.service.*;
import com.oa.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BusinessOrderServiceImpl extends ServiceImpl<BusinessOrderMapper, BusinessOrder> implements IBusinessOrderService {

    @Resource
    private ISysUserService sysUserService;
    @Resource
    private IApprovalSubmissionRecordService approvalSubmissionRecordService;
    @Resource
    private FlowableService flowableService;
    @Resource
    private IBusinessOrderItemService businessOrderItemService;
    @Resource
    private IBusinessOrderRefService businessOrderRefService;

    @MultiTransactional
    @Override
    public void add(BusinessOrderSaveDto saveDto) {
        List<Integer> bizTypeList = saveDto.getBizTypeList();
        if (bizTypeList.stream().anyMatch(x -> !BusinessOrderItemBizTypeEnum.codeMap.containsKey(x))) {
            throw new ServiceException(BaseCode.DATA_NOT_EXIST);
        }
        List<Long> refIds = saveDto.getRefIds();
        boolean refIdsEmptyFlag = CollectionUtils.isEmpty(refIds);
        if (!refIdsEmptyFlag) {
            List<BusinessOrder> orderList = listByIds(refIds).stream().filter(x -> DeletedEnum.UN_DELETE.getCode().equals(x.getDeleted())).collect(Collectors.toList());
            if (orderList.size() != refIds.size()) {
                throw new ServiceException(BaseCode.DATA_NOT_EXIST.getCode(), "关联合同不存在，请刷新页面后重试");
            }
        }
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
        entity.setFreeAmount(entity.getAmount());
        entity.setDeptId(SecurityUtils.getDeptId());
        entity.setCreateUser(userId);
        entity.setUpdateUser(userId);
        save(entity);
        businessOrderItemService.saveBatch(bizTypeList.stream()
                .map(x -> new BusinessOrderItem(entity.getId(), x, userId)).collect(Collectors.toList()));
        if (!refIdsEmptyFlag) {
            businessOrderRefService.saveBatch(refIds.stream().map(x -> new BusinessOrderRef(entity.getId(), x, userId)).collect(Collectors.toList()));
        }

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
    public TableDataInfo pageQuery(BusinessOrderQueryDto queryDto) {
        queryDto.setDataPermission(SecurityUtils.getLoginUser().getDataPermissionDto());
        Set<Integer> bizTypeSet = new HashSet<>();
        if (!Objects.isNull(queryDto.getBizType())) {
            bizTypeSet.add(queryDto.getBizType());
        }
        if (!Objects.isNull(queryDto.getMenuFlag())) {
            bizTypeSet.add(BusinessOrderItemBizTypeEnum.ACCOUNT_AGENCY.getCode());
            bizTypeSet.add(BusinessOrderItemBizTypeEnum.ACCOUNT_AGENCY_RENEW.getCode());
        }
        if (!CollectionUtils.isEmpty(bizTypeSet)) {
            List<BusinessOrderItem> itemList = businessOrderItemService.list(Wrappers.<BusinessOrderItem>lambdaQuery()
                    .in(BusinessOrderItem::getBizType, bizTypeSet)
                    .eq(BusinessOrderItem::getDeleted, DeletedEnum.UN_DELETE.getCode()));
            if (CollectionUtils.isEmpty(itemList)) {
                return new TableDataInfo();
            }
            queryDto.setIds(itemList.stream().map(BusinessOrderItem::getOrderId).collect(Collectors.toSet()));
        }
        Page<BusinessOrder> page = getBaseMapper().pageQuery(new Page<>(queryDto.getPageNum(), queryDto.getPageSize()), queryDto);
        List<BusinessOrder> list = page.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new TableDataInfo();
        }
        Map<Long, String> userMap = sysUserService.listByIds(list.stream().map(BusinessOrder::getCreateUser).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickName));
        boolean userMapEmptyFlag = CollectionUtils.isEmpty(userMap);
        List<Long> orderIds = list.stream().map(BusinessOrder::getId).collect(Collectors.toList());
        Map<Long, List<BusinessOrderItem>> itemMap = businessOrderItemService.selectListByOrderIds(orderIds)
                .stream().collect(Collectors.groupingBy(BusinessOrderItem::getOrderId));
        boolean itemMapEmptyFlag = CollectionUtils.isEmpty(itemMap);
        Map<Long, List<BusinessOrderRef>> refMap = businessOrderRefService.selectListByOrderIds(orderIds)
                .stream().collect(Collectors.groupingBy(BusinessOrderRef::getOrderId));
        boolean refMapEmptyFlag = CollectionUtils.isEmpty(refMap);
        Map<Long, BusinessOrder> refOrderMap = null;
        if (!refMapEmptyFlag) {
            refOrderMap = listByIds(refMap.values().stream().flatMap(Collection::stream).map(BusinessOrderRef::getRefId).collect(Collectors.toSet()))
                    .stream().collect(Collectors.toMap(BusinessOrder::getId, Function.identity()));
        }
        Map<Long, BusinessOrder> refOrderMapRef = refOrderMap;
        boolean refOrderMapRefEmptyFlag = CollectionUtils.isEmpty(refOrderMapRef);
        return new TableDataInfo(list.stream().map(x -> {
            BusinessOrderDetailVo resultVo = OrikaMapperUtils.map(x, BusinessOrderDetailVo.class);
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
            resultVo.setItemList(Collections.emptyList());
            List<BusinessOrderItem> itemList;
            if (!itemMapEmptyFlag && !CollectionUtils.isEmpty(itemList = itemMap.get(x.getId()))) {
                resultVo.setItemList(itemList.stream().map(item -> {
                    BusinessOrderItemBizTypeEnum bizTypeEnum = BusinessOrderItemBizTypeEnum.codeMap.get(item.getBizType());
                    String bizTypeName = StringUtils.EMPTY;
                    if (!Objects.isNull(bizTypeEnum)) {
                        bizTypeName = bizTypeEnum.getDesc();
                    }
                    return new BusinessOrderItemDetailVo(item.getId(), item.getOrderId(), item.getBizType(), bizTypeName);
                }).collect(Collectors.toList()));
            }
            resultVo.setRefOrderList(Collections.emptyList());
            if (!refMapEmptyFlag && !refOrderMapRefEmptyFlag) {
                List<BusinessOrderRef> orderRefList = refMap.get(x.getId());
                if (CollectionUtils.isEmpty(orderRefList)) {
                    return resultVo;
                }
                resultVo.setRefOrderList(orderRefList.stream().map(y -> {
                    BusinessOrder businessOrder = refOrderMapRef.get(y.getRefId());
                    if (Objects.isNull(businessOrder)) {
                        return null;
                    }
                    return OrikaMapperUtils.map(businessOrder, BusinessOrderShortVo.class);
                }).filter(Objects::nonNull).collect(Collectors.toList()));
            }
            return resultVo;
        }).collect(Collectors.toList()), page.getTotal());
    }

    @MultiTransactional
    @Override
    public void modify(BusinessOrderUpdDto updDto) {
        List<Integer> bizTypeList = updDto.getBizTypeList();
        if (bizTypeList.stream().anyMatch(x -> !BusinessOrderItemBizTypeEnum.codeMap.containsKey(x))) {
            throw new ServiceException(BaseCode.DATA_NOT_EXIST);
        }
        boolean refIdsEmptyFlag = CollectionUtils.isEmpty(updDto.getRefIds());
        if (!refIdsEmptyFlag) {
            List<BusinessOrder> orderList = listByIds(updDto.getRefIds()).stream().filter(x -> DeletedEnum.UN_DELETE.getCode().equals(x.getDeleted())).collect(Collectors.toList());
            if (orderList.size() != updDto.getRefIds().size()) {
                throw new ServiceException(BaseCode.DATA_NOT_EXIST.getCode(), "关联合同不存在，请刷新页面后重试");
            }
        }
        Long userId = SecurityUtils.getUserId();
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
        entity.setUsedAmount(BigDecimal.ZERO);
        entity.setFreeAmount(entity.getAmount());
        entity.setUpdateUser(userId);
        entity.setUpdateTime(new Date());
        entity.setApprovalStatus(ApprovalSubmissionRecordStatusEnum.AUDIT.getCode());
        updateById(entity);
        businessOrderItemService.update(Wrappers.<BusinessOrderItem>lambdaUpdate()
                .set(BusinessOrderItem::getDeleted, DeletedEnum.DELETED.getCode())
                .set(BusinessOrderItem::getUpdateUser, userId)
                .eq(BusinessOrderItem::getOrderId, entity.getId()));
        businessOrderItemService.saveBatch(bizTypeList.stream()
                .map(x -> new BusinessOrderItem(entity.getId(), x, userId)).collect(Collectors.toList()));
        businessOrderRefService.update(Wrappers.<BusinessOrderRef>lambdaUpdate()
                .set(BusinessOrderRef::getDeleted, DeletedEnum.DELETED.getCode())
                .set(BusinessOrderRef::getUpdateUser, userId)
                .eq(BusinessOrderRef::getOrderId, updDto.getId()));
        if (!refIdsEmptyFlag) {
            businessOrderRefService.saveBatch(updDto.getRefIds().stream().map(x -> new BusinessOrderRef(entity.getId(), x, userId)).collect(Collectors.toList()));
        }
        flowableService.invokeProcessResubmitAfter(entity.getId(), AuditTypeEnum.APPROVAL_BUSINESS_ORDER, updDto.getRemark());
    }

    @MultiTransactional
    @Override
    public void del(List<Long> ids) {
        Long userId = SecurityUtils.getUserId();
        businessOrderRefService.update(Wrappers.<BusinessOrderRef>lambdaUpdate()
                .set(BusinessOrderRef::getDeleted, DeletedEnum.DELETED.getCode())
                .set(BusinessOrderRef::getUpdateUser, userId)
                .in(BusinessOrderRef::getOrderId, ids));
        businessOrderItemService.update(Wrappers.<BusinessOrderItem>lambdaUpdate()
                .set(BusinessOrderItem::getDeleted, DeletedEnum.DELETED.getCode())
                .set(BusinessOrderItem::getUpdateUser, userId)
                .in(BusinessOrderItem::getOrderId, ids));
        update(Wrappers.<BusinessOrder>lambdaUpdate()
                .set(BusinessOrder::getDeleted, DeletedEnum.DELETED.getCode())
                .set(BusinessOrder::getUpdateUser, userId)
                .in(BusinessOrder::getId, ids));
        flowableService.batchDelProcess(AuditTypeEnum.APPROVAL_BUSINESS_ORDER, ids);
        approvalSubmissionRecordService.update(new LambdaUpdateWrapper<ApprovalSubmissionRecord>()
                .in(ApprovalSubmissionRecord::getBizId, ids)
                .eq(ApprovalSubmissionRecord::getAuditType, AuditTypeEnum.APPROVAL_BUSINESS_ORDER.getCode())
                .set(ApprovalSubmissionRecord::getDeleted, DeletedEnum.DELETED.getCode()));
    }
}
