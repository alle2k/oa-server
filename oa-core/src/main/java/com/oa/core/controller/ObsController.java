package com.oa.core.controller;

import com.oa.common.core.controller.BaseController;
import com.oa.common.core.domain.AjaxResult;
import com.oa.common.utils.ObsUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Api("Obs文件上传")
@Slf4j
@RestController
@RequestMapping("/obs")
public class ObsController extends BaseController {

    @PostMapping(value = "/upload")
    @ApiOperation("文件上传")
    public AjaxResult upload(MultipartFile file,
                             @RequestParam(value = "folder", required = false) String folder) {
        InputStream input = null;
        try {
            input = new ByteArrayInputStream(file.getBytes());
        } catch (Exception e) {
            log.error("上传文件失败", e);
        }
        return success(ObsUtils.upload(input, folder, file.getOriginalFilename()));
    }
}
