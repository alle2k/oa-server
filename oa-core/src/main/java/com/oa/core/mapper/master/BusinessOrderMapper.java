package com.oa.core.mapper.master;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.core.domain.BusinessOrder;
import com.oa.core.model.dto.BusinessOrderQueryDto;
import org.apache.ibatis.annotations.Param;

public interface BusinessOrderMapper extends BaseMapper<BusinessOrder> {

    Page<BusinessOrder> pageQuery(Page<?> page, @Param("param") BusinessOrderQueryDto queryDto);
}
