package com.oa.core.controller;

import com.oa.common.annotation.RepeatSubmit;
import com.oa.common.core.controller.BaseController;
import com.oa.common.core.domain.AjaxResult;
import com.oa.common.core.page.TableDataInfo;
import com.oa.core.model.dto.BusinessOrderQueryDto;
import com.oa.core.model.dto.BusinessOrderSaveDto;
import com.oa.core.model.dto.BusinessOrderUpdDto;
import com.oa.core.service.IBusinessOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@RestController
@RequestMapping("/order")
public class BusinessOrderController extends BaseController {

    @Resource
    private IBusinessOrderService businessOrderService;

    @GetMapping("/pageQuery")
    public TableDataInfo pageQuery(BusinessOrderQueryDto queryDto) {
        return businessOrderService.pageQuery(queryDto);
    }

    @RepeatSubmit
    @PostMapping
    public AjaxResult save(@Validated @RequestBody BusinessOrderSaveDto saveDto) {
        businessOrderService.add(saveDto);
        return success();
    }

    @RepeatSubmit
    @PutMapping
    public AjaxResult modify(@Validated @RequestBody BusinessOrderUpdDto updDto) {
        businessOrderService.modify(updDto);
        return success();
    }

    @DeleteMapping
    public AjaxResult del(@Validated @NotEmpty @RequestBody List<Long> ids) {
        businessOrderService.del(ids);
        return success();
    }
}
