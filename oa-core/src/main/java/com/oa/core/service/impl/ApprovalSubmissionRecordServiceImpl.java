package com.oa.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.constant.TransactionConstant;
import com.oa.common.core.domain.entity.SysUser;
import com.oa.common.core.domain.model.LoginUser;
import com.oa.common.utils.OrikaMapperUtils;
import com.oa.common.utils.SecurityUtils;
import com.oa.common.utils.StringUtils;
import com.oa.core.domain.ApprovalSubmissionRecord;
import com.oa.core.domain.BusinessOrder;
import com.oa.core.domain.OrderAccountAgency;
import com.oa.core.enums.ApprovalSubmissionRecordStatusEnum;
import com.oa.core.enums.AuditTypeEnum;
import com.oa.core.helper.GenerateAuditNoHelper;
import com.oa.core.mapper.master.ApprovalSubmissionRecordMapper;
import com.oa.core.model.dto.ApprovalSubmissionRecordSaveDto;
import com.oa.core.model.dto.AuditCandidateDto;
import com.oa.core.model.vo.AccountAgencyDetailVo;
import com.oa.core.model.vo.BizDetailVo;
import com.oa.core.model.vo.BusinessOrderDetailVo;
import com.oa.core.model.vo.UserShortVo;
import com.oa.core.service.FlowableService;
import com.oa.core.service.IApprovalSubmissionRecordService;
import com.oa.core.service.IBusinessOrderService;
import com.oa.core.service.IOrderAccountAgencyService;
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
