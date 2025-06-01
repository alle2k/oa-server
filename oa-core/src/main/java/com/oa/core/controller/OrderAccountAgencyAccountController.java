package com.oa.core.controller;


import com.oa.common.core.domain.AjaxResult;
import com.oa.common.core.page.TableDataInfo;
import com.oa.core.model.dto.AccountAgencyAccountQueryDto;
import com.oa.core.service.IOrderAccountAgencyAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @auther CodeGenerator
 * @create 2025-05-31 17:05:33
 * @describe 代理记账台账前端控制器
 */
@Slf4j
@RestController
@RequestMapping("/orderAccountAgencyAccount")
public class OrderAccountAgencyAccountController {

    @Resource
    private IOrderAccountAgencyAccountService orderAccountAgencyAccountService;

    @GetMapping("/pageQuery")
    public TableDataInfo pageQuery(AccountAgencyAccountQueryDto dto) {
        return orderAccountAgencyAccountService.pageQuery(dto);
    }

    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id) {
        return AjaxResult.success(orderAccountAgencyAccountService.detail(id));
    }

    @GetMapping("/getOrderAccountAgencyDetailsByOrderId")
    public AjaxResult getOrderAccountAgencyDetailsByOrderId(@RequestParam Long id) {
        return AjaxResult.success(orderAccountAgencyAccountService.getOrderAccountAgencyDetailsByOrderId(id));
    }
}
