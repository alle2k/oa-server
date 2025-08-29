package com.oa.core.controller;

import com.oa.common.annotation.RepeatSubmit;
import com.oa.common.core.controller.BaseController;
import com.oa.common.core.domain.AjaxResult;
import com.oa.common.core.redis.RedisCache;
import com.oa.common.exception.ServiceException;
import com.oa.core.config.FlowableSpecialApprovalConfig;
import com.oa.core.model.dto.FlowableAuditParam;
import com.oa.core.model.dto.FlowableRemindParam;
import com.oa.core.model.dto.FlowableTaskCallbackParam;
import com.oa.core.model.dto.FlowableTaskTransferParam;
import com.oa.core.service.FlowableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/flowable")
public class FlowableController extends BaseController {

    @Resource
    private FlowableService flowableService;
    @Resource
    private RedisCache redisCache;

    @PostMapping("/deploy")
    public AjaxResult deploy(@NotBlank String fileName, String deployName, @NotBlank String deployKey) {
        flowableService.deploy(fileName, deployName, deployKey);
        return success();
    }

    @RepeatSubmit(interval = 1000)
    @PostMapping("/audit")
    public AjaxResult audit(@Validated @RequestBody FlowableAuditParam param) {
        String lockKey = "audit:" + param.getAuditType() + ":" + param.getId();
        boolean flag = false;
        try {
            flag = redisCache.setnx(lockKey, "1", 15L, TimeUnit.SECONDS);
            if (!flag) {
                throw new ServiceException("当前节点正在审批中");
            }
            flowableService.audit(param);
        } finally {
            if (flag) {
                redisCache.deleteObject(lockKey);
            }
        }
        return success();
    }

    @RepeatSubmit
    @PostMapping("/transfer")
    public AjaxResult transfer(@Validated @RequestBody FlowableTaskTransferParam param) {
        String lockKey = "audit:" + param.getAuditType() + ":" + param.getId();
        boolean flag = false;
        try {
            flag = redisCache.setnx(lockKey, "1", 15L, TimeUnit.SECONDS);
            if (!flag) {
                throw new ServiceException("当前节点正在审批中");
            }
            flowableService.transfer(param);
        } finally {
            if (flag) {
                redisCache.deleteObject(lockKey);
            }
        }
        return success();
    }

    @RepeatSubmit
    @PostMapping("/rollback")
    public AjaxResult rollback(@Validated @RequestBody FlowableTaskCallbackParam param) {
        String lockKey = "audit:" + param.getAuditType() + ":" + param.getId();
        boolean flag = false;
        try {
            flag = redisCache.setnx(lockKey, "1", 15L, TimeUnit.SECONDS);
            if (!flag) {
                throw new ServiceException("当前节点正在审批中");
            }
            flowableService.rollback(param);
        } finally {
            if (flag) {
                redisCache.deleteObject(lockKey);
            }
        }
        return success();
    }

    @RepeatSubmit
    @PostMapping("/remind")
    public AjaxResult remind(@RequestBody @Validated FlowableRemindParam param) {
        flowableService.remind(param.getId(), param.getAuditType());
        return success();
    }

    @GetMapping("/getSpecialApprovalFormFields")
    public AjaxResult getSpecialApprovalFormFields() {
        return success(FlowableSpecialApprovalConfig.SPECIAL_APPROVAL_FORM_MAP);
    }
}
