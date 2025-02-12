package com.oa.core.controller;

import com.oa.common.annotation.Anonymous;
import com.oa.common.annotation.RepeatSubmit;
import com.oa.common.core.domain.AjaxResult;
import com.oa.core.model.dto.BusinessOrderSaveDto;
import com.oa.core.service.IBusinessOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/order")
public class BusinessOrderController {

    @Resource
    private IBusinessOrderService businessOrderService;

    @Anonymous
    @GetMapping("/pageQuery")
    public AjaxResult pageQuery() {
        return AjaxResult.success();
    }

    @RepeatSubmit
    @PostMapping
    public AjaxResult save(@Validated @RequestBody BusinessOrderSaveDto saveDto) {
        businessOrderService.add(saveDto);
        return AjaxResult.success();
    }
}
