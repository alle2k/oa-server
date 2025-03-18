package com.oa.core.controller;

import com.oa.common.core.controller.BaseController;
import com.oa.common.core.domain.AjaxResult;
import com.oa.common.core.page.TableDataInfo;
import com.oa.core.model.dto.AccountAgencyQueryDto;
import com.oa.core.model.dto.OrderAccountAgencySaveDto;
import com.oa.core.model.dto.OrderAccountAgencyUpdDtp;
import com.oa.core.service.IOrderAccountAgencyService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@RestController
@RequestMapping("/accountAgency")
public class AccountAgencyController extends BaseController {

    @Resource
    private IOrderAccountAgencyService orderAccountAgencyService;

    @GetMapping("/pageQuery")
    public TableDataInfo pageQuery(AccountAgencyQueryDto dto) {
        return orderAccountAgencyService.pageQuery(dto);
    }

    @PostMapping
    public AjaxResult add(@RequestBody @Validated OrderAccountAgencySaveDto dto) {
        orderAccountAgencyService.add(dto);
        return success();
    }

    @PutMapping
    public AjaxResult modify(@RequestBody @Validated OrderAccountAgencyUpdDtp dto) {
        orderAccountAgencyService.modify(dto);
        return success();
    }

    @DeleteMapping("/{id}")
    public AjaxResult del(@PathVariable Long id) {
        orderAccountAgencyService.del(id);
        return success();
    }

    @DeleteMapping
    public AjaxResult del(@Validated @NotEmpty @RequestBody List<Long> ids) {
        orderAccountAgencyService.del(ids);
        return success();
    }
}
