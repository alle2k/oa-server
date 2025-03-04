package com.oa.core.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.core.domain.BusinessOrderItem;
import com.oa.core.enums.DeletedEnum;

import java.util.Collection;
import java.util.List;

/**
 * @auther CodeGenerator
 * @create 2025-03-04 11:40:47
 * @describe 服务类
 */
public interface IBusinessOrderItemService extends IService<BusinessOrderItem> {

    default List<BusinessOrderItem> selectListByOrderIds(Collection<Long> orderIds) {
        return list(Wrappers.<BusinessOrderItem>lambdaQuery()
                .in(BusinessOrderItem::getOrderId, orderIds)
                .eq(BusinessOrderItem::getDeleted, DeletedEnum.UN_DELETE.getCode()));
    }
}
