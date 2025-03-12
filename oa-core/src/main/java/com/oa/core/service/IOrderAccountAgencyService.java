package com.oa.core.service;

import com.oa.core.domain.OrderAccountAgency;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.core.model.dto.OrderAccountAgencySaveDto;

/**
 * @auther CodeGenerator
 * @create 2025-03-12 19:05:13
 * @describe 代理记账服务类
 */
public interface IOrderAccountAgencyService extends IService<OrderAccountAgency> {

    void add(OrderAccountAgencySaveDto dto);

    void del(Long id);
}
