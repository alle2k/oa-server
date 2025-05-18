package com.oa.core.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.common.enums.DeletedEnum;
import com.oa.core.domain.BusinessOrderRef;

import java.util.Collection;
import java.util.List;

/**
 * @auther CodeGenerator
 * @create 2025-05-02 15:28:39
 * @describe 合同关联存量订单表服务类
 */
public interface IBusinessOrderRefService extends IService<BusinessOrderRef> {

    default List<BusinessOrderRef> selectListByOrderIds(Collection<Long> orderIds) {
        return list(Wrappers.<BusinessOrderRef>lambdaQuery()
                .in(BusinessOrderRef::getOrderId, orderIds)
                .eq(BusinessOrderRef::getDeleted, DeletedEnum.UN_DELETE.getCode()));
    }
}
