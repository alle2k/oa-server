package com.oa.core.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.common.core.page.TableDataInfo;
import com.oa.common.enums.DeletedEnum;
import com.oa.common.error.BaseCode;
import com.oa.common.exception.ServiceException;
import com.oa.core.domain.OrderAccountAgencyAccount;
import com.oa.core.model.dto.AccountAgencyAccountQueryDto;
import com.oa.core.model.vo.AccountAgencyAccountDetailVo;
import com.oa.core.model.vo.OrderAccountAgencyDetailVo;

import java.util.List;
import java.util.Objects;

/**
 * @auther CodeGenerator
 * @create 2025-05-31 17:05:33
 * @describe 代理记账台账服务类
 */
public interface IOrderAccountAgencyAccountService extends IService<OrderAccountAgencyAccount> {

    TableDataInfo pageQuery(AccountAgencyAccountQueryDto dto);

    AccountAgencyAccountDetailVo detail(Long id);

    default OrderAccountAgencyAccount selectOneById(Long id) {
        OrderAccountAgencyAccount entity = getById(id);
        if (Objects.isNull(entity) || DeletedEnum.DELETED.getCode().equals(entity.getDeleted())) {
            throw new ServiceException(BaseCode.DATA_NOT_EXIST);
        }
        return entity;
    }

    default OrderAccountAgencyAccount selectOneByOrderId(Long orderId) {
        OrderAccountAgencyAccount entity = getOne(Wrappers.<OrderAccountAgencyAccount>lambdaQuery()
                .eq(OrderAccountAgencyAccount::getOrderId, orderId)
                .eq(OrderAccountAgencyAccount::getDeleted, DeletedEnum.UN_DELETE.getCode()));
        if (Objects.isNull(entity) || DeletedEnum.DELETED.getCode().equals(entity.getDeleted())) {
            throw new ServiceException(BaseCode.DATA_NOT_EXIST);
        }
        return entity;
    }

    List<OrderAccountAgencyDetailVo> getOrderAccountAgencyDetailsByOrderId(Long id);
}
