package com.oa.core.mapper.master;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.core.domain.OrderAccountAgency;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.core.model.dto.AccountAgencyQueryDto;
import com.oa.core.model.vo.AccountAgencyDetailVo;
import org.apache.ibatis.annotations.Param;

/**
 * @auther CodeGenerator
 * @create 2025-03-12 19:05:13
 * @describe 代理记账mapper类
 */
public interface OrderAccountAgencyMapper extends BaseMapper<OrderAccountAgency> {

    Page<AccountAgencyDetailVo> pageQuery(Page<?> page, @Param("param") AccountAgencyQueryDto dto);
}
