package com.oa.core.controller;

import com.oa.common.core.controller.BaseController;
import com.oa.common.core.domain.AjaxResult;
import com.oa.core.model.dto.OrderAccountAgencySaveDto;
import com.oa.core.service.IOrderAccountAgencyService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/accountAgency")
public class AccountAgencyController extends BaseController {

    @Resource
    private IOrderAccountAgencyService orderAccountAgencyService;

    @PostMapping
    public AjaxResult add(@RequestBody @Validated OrderAccountAgencySaveDto dto) {
        orderAccountAgencyService.add(dto);
        return success();
    }

    @DeleteMapping("/{id}")
    public AjaxResult del(@PathVariable Long id) {
        orderAccountAgencyService.del(id);
        return success();
    }
}
