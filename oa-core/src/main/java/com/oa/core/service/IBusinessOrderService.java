package com.oa.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.common.core.page.TableDataInfo;
import com.oa.common.enums.DeletedEnum;
import com.oa.common.error.BaseCode;
import com.oa.common.exception.ServiceException;
import com.oa.core.domain.BusinessOrder;
import com.oa.core.model.dto.BusinessOrderQueryDto;
import com.oa.core.model.dto.BusinessOrderSaveDto;
import com.oa.core.model.dto.BusinessOrderUpdDto;

import java.util.List;
import java.util.Objects;

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
     * @return TableDataInfo
     */
    TableDataInfo pageQuery(BusinessOrderQueryDto queryDto);

    /**
     * 修改订单
     *
     * @param updDto 业务信息
     */
    void modify(BusinessOrderUpdDto updDto);

    /**
     * 根据ID获取订单信息
     *
     * @param id PK
     * @return BusinessOrder
     */
    default BusinessOrder selectOneById(Long id) {
        BusinessOrder entity = getById(id);
        if (Objects.isNull(entity) || DeletedEnum.DELETED.getCode().equals(entity.getDeleted())) {
            throw new ServiceException(BaseCode.DATA_NOT_EXIST);
        }
        return entity;
    }

    /**
     * 删除合同订单
     *
     * @param ids 订单ID列表
     */
    void del(List<Long> ids);
}
