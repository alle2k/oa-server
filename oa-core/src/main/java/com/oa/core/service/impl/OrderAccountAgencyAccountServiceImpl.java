package com.oa.core.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.core.domain.entity.SysUser;
import com.oa.common.core.page.TableDataInfo;
import com.oa.common.utils.OrikaMapperUtils;
import com.oa.common.utils.StringUtils;
import com.oa.core.domain.*;
import com.oa.core.enums.ApprovalSubmissionRecordStatusEnum;
import com.oa.core.enums.AuditTypeEnum;
import com.oa.core.enums.BusinessOrderItemBizTypeEnum;
import com.oa.core.mapper.master.OrderAccountAgencyAccountMapper;
import com.oa.core.model.dto.AccountAgencyAccountQueryDto;
import com.oa.core.model.vo.AccountAgencyAccountDetailVo;
import com.oa.core.model.vo.OrderAccountAgencyDetailVo;
import com.oa.core.service.*;
import com.oa.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @auther CodeGenerator
 * @create 2025-05-31 17:05:33
 * @describe 代理记账台账服务实现类
 */
@Service
public class OrderAccountAgencyAccountServiceImpl extends ServiceImpl<OrderAccountAgencyAccountMapper, OrderAccountAgencyAccount> implements IOrderAccountAgencyAccountService {

    @Resource
    private IBusinessOrderService businessOrderService;
    @Resource
    private IBusinessOrderRefService businessOrderRefService;
    @Resource
    private IBusinessOrderItemService businessOrderItemService;
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private IOrderAccountAgencyService orderAccountAgencyService;

    @Override
    public TableDataInfo pageQuery(AccountAgencyAccountQueryDto dto) {
        Page<AccountAgencyAccountDetailVo> page = getBaseMapper().pageQuery(new Page<>(dto.getPageNum(), dto.getPageSize()), dto);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new TableDataInfo();
        }
        Map<Long, String> userMap = sysUserService.listByIds(page.getRecords().stream().map(AccountAgencyAccountDetailVo::getCreateUser).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickName));
        boolean userMapEmptyFlag = CollectionUtils.isEmpty(userMap);
        page.getRecords().forEach(x -> {
            x.setCreateUserName(StringUtils.EMPTY);
            if (!userMapEmptyFlag) {
                x.setCreateUserName(userMap.getOrDefault(x.getCreateUser(), StringUtils.EMPTY));
            }
        });
        return new TableDataInfo(page.getRecords(), page.getTotal());
    }

    @Override
    public AccountAgencyAccountDetailVo detail(Long id) {
        OrderAccountAgencyAccount accountAgency = selectOneById(id);
        AccountAgencyAccountDetailVo agencyDetailVo = OrikaMapperUtils.map(accountAgency, AccountAgencyAccountDetailVo.class);
        agencyDetailVo.setAnnexUrlList(StringUtils.str2List(accountAgency.getAnnexUrl()));
        agencyDetailVo.setPaymentScreenshotList(StringUtils.str2List(accountAgency.getPaymentScreenshot()));
        return agencyDetailVo;
    }

    @Override
    public List<OrderAccountAgencyDetailVo> getOrderAccountAgencyDetailsByOrderId(Long id) {
        List<BusinessOrderRef> refOrderList = businessOrderRefService.selectListByRefIds(Collections.singleton(id));
        List<Long> orderIds = new LinkedList<>();
        orderIds.add(id);
        if (!CollectionUtils.isEmpty(refOrderList)) {
            orderIds.addAll(refOrderList.stream().map(BusinessOrderRef::getOrderId).collect(Collectors.toSet()));
        }
        List<OrderAccountAgency> accountAgencyList = orderAccountAgencyService.selectListByOrderIds(orderIds).stream().filter(x -> ApprovalSubmissionRecordStatusEnum.PASS.getCode() == x.getApprovalStatus()).collect(Collectors.toList());
        boolean accountAgencyListEmptyFlag = CollectionUtils.isEmpty(accountAgencyList);
        List<BusinessOrder> orderList = businessOrderService.listByIds(orderIds);
        Set<Long> userIdSet = orderList.stream().map(BusinessOrder::getCreateUser).collect(Collectors.toSet());
        if (!accountAgencyListEmptyFlag) {
            userIdSet.addAll(accountAgencyList.stream().map(OrderAccountAgency::getCreateUser).collect(Collectors.toSet()));
        }
        Map<Long, SysUser> userMap = sysUserService.listByIds(userIdSet)
                .stream().collect(Collectors.toMap(SysUser::getUserId, Function.identity()));
        List<OrderAccountAgencyDetailVo> resultList = new LinkedList<>();
        if (!accountAgencyListEmptyFlag) {
            resultList.addAll(accountAgencyList.stream().map(x -> {
                OrderAccountAgencyDetailVo vo = new OrderAccountAgencyDetailVo(x.getId(), AuditTypeEnum.APPROVAL_ACCOUNT_AGENCY.getCode(), x.getOrderId(), x.getCreateTime(), x.getApprovalTime(), StringUtils.EMPTY, x.getCreateUser(), StringUtils.EMPTY, x.getAmount(), StringUtils.EMPTY);
                SysUser sysUser = userMap.get(vo.getCreateUser());
                if (!Objects.isNull(sysUser)) {
                    vo.setCreateUserName(sysUser.getNickName());
                    vo.setAvatar(sysUser.getAvatar());
                }
                return vo;
            }).collect(Collectors.toList()));
        }
        resultList.addAll(orderList.stream().map(x -> {
            OrderAccountAgencyDetailVo vo = new OrderAccountAgencyDetailVo(x.getId(), AuditTypeEnum.APPROVAL_BUSINESS_ORDER.getCode(), x.getId(), x.getCreateTime(), x.getApprovalTime(), StringUtils.EMPTY, x.getCreateUser(), StringUtils.EMPTY, x.getAmount(), StringUtils.EMPTY);
            SysUser sysUser = userMap.get(vo.getCreateUser());
            if (!Objects.isNull(sysUser)) {
                vo.setCreateUserName(sysUser.getNickName());
                vo.setAvatar(sysUser.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList()));
        resultList.sort(Comparator.comparing(OrderAccountAgencyDetailVo::getCreateTime).reversed());
        Map<Long, List<BusinessOrderItem>> orderItemMap = businessOrderItemService.selectListByOrderIds(orderIds)
                .stream().collect(Collectors.groupingBy(BusinessOrderItem::getOrderId));
        resultList.forEach(x -> {
            if (AuditTypeEnum.APPROVAL_BUSINESS_ORDER.getCode().equals(x.getBizType())) {
                if (orderItemMap.get(x.getOrderId()).stream().anyMatch(y -> BusinessOrderItemBizTypeEnum.ACCOUNT_AGENCY.getCode() == y.getBizType())) {
                    x.setDesc("原订单合同");
                } else {
                    x.setDesc("续期合同");
                }
            } else {
                if (orderItemMap.get(x.getOrderId()).stream().anyMatch(y -> BusinessOrderItemBizTypeEnum.ACCOUNT_AGENCY.getCode() == y.getBizType())) {
                    x.setDesc("代理记账申请");
                } else {
                    x.setDesc("续期记账申请");
                }
            }
        });
        return resultList;
    }
}
