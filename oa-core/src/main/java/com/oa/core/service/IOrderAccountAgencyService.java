package com.oa.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.common.core.page.TableDataInfo;
import com.oa.common.enums.DeletedEnum;
import com.oa.common.error.BaseCode;
import com.oa.common.exception.ServiceException;
import com.oa.core.domain.OrderAccountAgency;
import com.oa.core.model.dto.AccountAgencyQueryDto;
import com.oa.core.model.dto.OrderAccountAgencySaveDto;
import com.oa.core.model.dto.OrderAccountAgencyUpdDtp;
import com.oa.core.model.vo.AccountAgencyDetailVo;

import java.util.List;
import java.util.Objects;

/**
 * @auther CodeGenerator
 * @create 2025-03-12 19:05:13
 * @describe 代理记账服务类
 */
public interface IOrderAccountAgencyService extends IService<OrderAccountAgency> {

    /**
     * 添加新的代理记账记录
     *
     * @param dto 代理记账保存数据传输对象
     */
    void add(OrderAccountAgencySaveDto dto);

    /**
     * 删除指定ID的代理记账记录
     *
     * @param id 代理记账记录ID
     */
    void del(Long id);

    /**
     * 批量删除指定ID的代理记账记录
     *
     * @param ids 代理记账记录ID
     */
    void del(List<Long> ids);

    /**
     * 分页查询代理记账记录
     *
     * @param dto 代理记账查询数据传输对象
     * @return 分页数据信息
     */
    TableDataInfo pageQuery(AccountAgencyQueryDto dto);


    /**
     * 根据ID查询代理记账记录
     *
     * @param id 代理记账记录ID
     * @return 代理记账记录实体
     * @throws ServiceException 如果记录不存在或已被删除，则抛出异常
     */
    default OrderAccountAgency selectOneById(Long id) {
        OrderAccountAgency entity = getById(id);
        if (Objects.isNull(entity) || DeletedEnum.DELETED.getCode().equals(entity.getDeleted())) {
            throw new ServiceException(BaseCode.DATA_NOT_EXIST);
        }
        return entity;
    }

    void modify(OrderAccountAgencyUpdDtp dto);

    AccountAgencyDetailVo detail(Long id);
}
