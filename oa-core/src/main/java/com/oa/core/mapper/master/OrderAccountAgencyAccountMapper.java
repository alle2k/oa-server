package com.oa.core.mapper.master;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.core.domain.OrderAccountAgencyAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.core.model.dto.AccountAgencyAccountQueryDto;
import com.oa.core.model.vo.AccountAgencyAccountDetailVo;
import org.apache.ibatis.annotations.Param;

/**
 * @auther CodeGenerator
 * @create 2025-05-31 17:05:33
 * @describe 代理记账台账mapper类
 */
public interface OrderAccountAgencyAccountMapper extends BaseMapper<OrderAccountAgencyAccount> {

    Page<AccountAgencyAccountDetailVo> pageQuery(Page<?> objectPage, @Param("param") AccountAgencyAccountQueryDto dto);
}
