package com.oa.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.constant.TransactionConstant;
import com.oa.common.core.domain.entity.SysDept;
import com.oa.common.core.domain.entity.SysUser;
import com.oa.common.core.domain.model.LoginUser;
import com.oa.common.utils.OrikaMapperUtils;
import com.oa.common.utils.SecurityUtils;
import com.oa.common.utils.StringUtils;
import com.oa.core.domain.*;
import com.oa.core.enums.ApprovalSubmissionRecordStatusEnum;
import com.oa.core.enums.AuditTypeEnum;
import com.oa.core.enums.BusinessOrderItemBizTypeEnum;
import com.oa.core.helper.GenerateAuditNoHelper;
import com.oa.core.mapper.master.ApprovalSubmissionRecordMapper;
import com.oa.core.model.dto.ApprovalSubmissionRecordSaveDto;
import com.oa.core.model.dto.AuditCandidateDto;
import com.oa.core.model.vo.*;
import com.oa.core.service.*;
import com.oa.system.service.ISysDeptService;
import com.oa.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApprovalSubmissionRecordServiceImpl extends ServiceImpl<ApprovalSubmissionRecordMapper, ApprovalSubmissionRecord> implements IApprovalSubmissionRecordService {

    @Resource
    private GenerateAuditNoHelper generateAuditNoHelper;
    @Resource
    private FlowableService flowableService;
    @Resource
    private IBusinessOrderService businessOrderService;
    @Resource
    private IOrderAccountAgencyService orderAccountAgencyService;
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private ISysDeptService sysDeptService;
    @Resource
    private IBusinessOrderItemService businessOrderItemService;
    @Resource
    private IBusinessOrderRefService businessOrderRefService;

    @Transactional(TransactionConstant.MASTER)
    @Override
    public String add(ApprovalSubmissionRecordSaveDto record) {
        Date date = new Date();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        ApprovalSubmissionRecord approvalRecord = new ApprovalSubmissionRecord();
        approvalRecord.setAuditType(record.getAuditTypeEnum().getCode());
        approvalRecord.setInstanceId(record.getInstanceId());
        approvalRecord.setApprovalTime(date);
        approvalRecord.setApplyUserId(loginUser.getUserId());
        approvalRecord.setBizId(record.getBizId());
        approvalRecord.setCreateUser(loginUser.getUserId());
        approvalRecord.setApprovalStatus(ApprovalSubmissionRecordStatusEnum.AUDIT.getCode());
        approvalRecord.setRemark(record.getRemark());
        // 获取当前登录人部门id
        approvalRecord.setApplyUserDeptId(String.valueOf(loginUser.getDeptId()));
        approvalRecord.setAuditNo(String.format("%s-%s", record.getAuditTypeEnum().getPrefix(), generateAuditNoHelper.get()));
        save(approvalRecord);
        return approvalRecord.getAuditNo();
    }

    @Override
    public BizDetailVo<?> getBizDetailByBizIdAndAuditType(Long bizId, Integer auditType) {
        BizDetailVo<?> result;
        AuditTypeEnum auditTypeEnum = AuditTypeEnum.codeMap.get(auditType);
        ApprovalSubmissionRecord approvalSubmissionRecord = selectByBizIdAndAuditType(bizId, auditTypeEnum);
        if (Objects.isNull(approvalSubmissionRecord)) {
            return null;
        }
        switch (auditTypeEnum) {
            case APPROVAL_BUSINESS_ORDER:
                BusinessOrder businessOrder = businessOrderService.selectOneById(bizId);
                BusinessOrderDetailVo businessOrderDetailVo = OrikaMapperUtils.map(businessOrder, BusinessOrderDetailVo.class);
                businessOrderDetailVo.setAnnexUrlList(StringUtils.str2List(businessOrderDetailVo.getAnnexUrl()));
                businessOrderDetailVo.setPaymentScreenshotList(StringUtils.str2List(businessOrderDetailVo.getPaymentScreenshot()));
                SysUser user = sysUserService.selectOneByUserId(businessOrder.getCreateUser());
                businessOrderDetailVo.setCreateUserName(user.getNickName());
                businessOrderDetailVo.setCreateUserDeptId(user.getDeptId());
                SysDept sysDept = sysDeptService.selectOneByDeptId(user.getDeptId());
                businessOrderDetailVo.setCreateUserDeptName(sysDept.getDeptName());
                businessOrderDetailVo.setCreateUserFullDeptId(sysDept.getAncestors());
                businessOrderDetailVo.setCreateUserFullDeptName(businessOrderDetailVo.getCreateUserDeptName());
                String[] parentDeptIdArr = sysDept.getAncestors().split(",");
                Map<Long, String> parentDeptNameMap = sysDeptService.listByIds(Arrays.stream(parentDeptIdArr).map(Long::valueOf).collect(Collectors.toSet()))
                        .stream().collect(Collectors.toMap(SysDept::getDeptId, SysDept::getDeptName));
                if (!CollectionUtils.isEmpty(parentDeptNameMap)) {
                    businessOrderDetailVo.setCreateUserFullDeptName(Arrays.stream(parentDeptIdArr).map(x -> parentDeptNameMap.get(Long.valueOf(x)))
                            .filter(StringUtils::isNotBlank).collect(Collectors.joining("-")));
                }
                List<BusinessOrderItem> itemList = businessOrderItemService.selectListByOrderIds(Collections.singleton(bizId));
                businessOrderDetailVo.setItemList(Collections.emptyList());
                if (!CollectionUtils.isEmpty(itemList)) {
                    businessOrderDetailVo.setItemList(itemList.stream().map(item -> {
                        BusinessOrderItemBizTypeEnum bizTypeEnum = BusinessOrderItemBizTypeEnum.codeMap.get(item.getBizType());
                        String bizTypeName;
                        if (!Objects.isNull(bizTypeEnum)) {
                            bizTypeName = bizTypeEnum.getDesc();
                        } else {
                            bizTypeName = StringUtils.EMPTY;
                        }
                        return new BusinessOrderItemDetailVo(item.getId(), item.getOrderId(), item.getBizType(), bizTypeName);
                    }).collect(Collectors.toList()));
                }
                List<BusinessOrderRef> orderRefList = businessOrderRefService.selectListByOrderIds(Collections.singleton(bizId));
                businessOrderDetailVo.setRefOrderList(Collections.emptyList());
                if (!CollectionUtils.isEmpty(orderRefList)) {
                    List<BusinessOrder> orderList = businessOrderService.listByIds(orderRefList.stream().map(BusinessOrderRef::getRefId).collect(Collectors.toSet()));
                    if (!CollectionUtils.isEmpty(orderList)) {
                        businessOrderDetailVo.setRefOrderList(OrikaMapperUtils.mapList(orderList, BusinessOrder.class, BusinessOrderShortVo.class));
                    }
                }
                result = new BizDetailVo<>(businessOrderDetailVo);
                break;
            case APPROVAL_ACCOUNT_AGENCY:
                OrderAccountAgency accountAgency = orderAccountAgencyService.selectOneById(bizId);
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
                result = new BizDetailVo<>(agencyDetailVo);
                break;
            default:
                result = new BizDetailVo<>();
        }
        result.setNodeInfo(flowableService.selectAllNodeInfo(approvalSubmissionRecord.getInstanceId()));
        result.setCurrentAuditUserList(Collections.emptyList());
        Map<String, Object> map = flowableService.selectCurrentTaskCandidateUser(approvalSubmissionRecord.getInstanceId());
        if (CollectionUtils.isEmpty(map)) {
            return result;
        }
        List<?> candidates = (List<?>) map.get("candidates");
        if (CollectionUtils.isEmpty(candidates)) {
            return result;
        }
        Set<Long> userIdSet = candidates.stream().map(x -> ((AuditCandidateDto) x).getUserId()).collect(Collectors.toSet());
        result.setCurrentAuditUserList(OrikaMapperUtils.mapList(sysUserService.listByIds(userIdSet), SysUser.class, UserShortVo.class));
        return result;
    }
}
