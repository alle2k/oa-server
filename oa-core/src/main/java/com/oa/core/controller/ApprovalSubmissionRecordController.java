package com.oa.core.controller;

import com.oa.common.core.controller.BaseController;
import com.oa.common.core.domain.AjaxResult;
import com.oa.core.service.IApprovalSubmissionRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "审批提交记录")
@RequestMapping("/approvalSubmissionRecord")
@RestController
public class ApprovalSubmissionRecordController extends BaseController {

    @Resource
    private IApprovalSubmissionRecordService approvalSubmissionRecordService;

    @ApiOperation("获取业务详情")
    @GetMapping("/getBizDetailByBizIdAndAuditType")
    public AjaxResult getBizDetailByBizIdAndAuditType(@ApiParam(required = true, value = "业务ID") @RequestParam Long bizId,
                                                      @RequestParam Integer auditType) {
        return success(approvalSubmissionRecordService.getBizDetailByBizIdAndAuditType(bizId, auditType));
    }
}
