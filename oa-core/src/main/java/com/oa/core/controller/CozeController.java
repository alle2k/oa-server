package com.oa.core.controller;

import com.oa.common.core.controller.BaseController;
import com.oa.common.core.domain.AjaxResult;
import com.oa.common.utils.SecurityUtils;
import com.oa.core.helper.CozeJWTOauthHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/coze")
public class CozeController extends BaseController {

    @Resource
    private CozeJWTOauthHelper cozeJWTOauthHelper;

    @GetMapping
    public AjaxResult getAccessToken() {
        return success(cozeJWTOauthHelper.getAccessToken(SecurityUtils.getUserId()));
    }
}
