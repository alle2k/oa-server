package com.oa.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.core.domain.BusinessOrder;
import com.oa.core.model.dto.BusinessOrderQueryDto;
import com.oa.core.model.dto.BusinessOrderSaveDto;
import com.oa.core.model.dto.BusinessOrderUpdDto;
import com.oa.core.model.vo.BusinessOrderShortVo;

import java.util.List;

public interface IBusinessOrderService extends IService<BusinessOrder> {

    /**
     * 保存订单
     *
     * @param saveDto 业务信息
     */
    void add(BusinessOrderSaveDto saveDto);

    /**
     * 分页查询订单
     *
     * @param queryDto 查询域条件
     * @return List
     */
    List<BusinessOrderShortVo> pageQuery(BusinessOrderQueryDto queryDto);

    /**
     * 修改订单
     *
     * @param updDto 业务信息
     */
    void modify(BusinessOrderUpdDto updDto);
}
