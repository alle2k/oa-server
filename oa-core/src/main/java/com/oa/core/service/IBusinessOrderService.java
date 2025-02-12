package com.oa.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.core.domain.BusinessOrder;
import com.oa.core.model.dto.BusinessOrderSaveDto;

public interface IBusinessOrderService extends IService<BusinessOrder> {

    /**
     * 保存订单
     *
     * @param saveDto 业务信息
     */
    void add(BusinessOrderSaveDto saveDto);
}
