package com.oa.common.utils;

import com.oa.common.config.properties.ObsProperties;
import com.oa.common.error.BaseCode;
import com.oa.common.exception.ServiceException;
import com.oa.common.utils.spring.SpringUtils;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ObsUtils {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 以输入流的形式上传文件
     */
    public static String upload(InputStream inputStream, String folder, String fileName) {
        suffixVerify(fileName);
        ObsClient obsClient = SpringUtils.getBean(ObsClient.class);
        ObsProperties properties = SpringUtils.getBean(ObsProperties.class);
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            // 指定该Object被下载时的网页的缓存行为
            metadata.setCacheControl("no-cache");
            /*if (fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(".pdf")) {
                metadata.setContentType("application/pdf");
                metadata.setContentDisposition("inline;filename=\"" + StringUtils.replace(URLEncoder.encode(fileName, "utf-8"), "+", "%20") + "\"");
            } else {
                metadata.setContentDisposition("attachment;filename=\"" + StringUtils.replace(URLEncoder.encode(fileName, "utf-8"), "+", "%20") + "\""); //兼容 同时防止空格变+
            }*/
            metadata.setContentDisposition("attachment;filename=\"" + StringUtils.replace(URLEncoder.encode(fileName, "utf-8"), "+", "%20") + "\""); //兼容 同时防止空格变+
            String ossFileName = genOssFileName(folder, fileName);
            PutObjectRequest request = new PutObjectRequest();
            request.setBucketName(properties.getBucketName());
            request.setObjectKey(ossFileName);
            request.setMetadata(metadata);
            request.setInput(inputStream);
            obsClient.putObject(request);
            return String.format("https://%s.%s/%s", properties.getBucketName(), properties.getEndPoint(), ossFileName);
        } catch (ObsException e) {
            log.error("putObject failed, HTTP Code:{}, Error Code:{}, Error Message: {}, Request ID:{}. Host ID: {}",
                    e.getResponseCode(), e.getErrorCode(), e.getErrorMessage(), e.getErrorRequestId(), e.getErrorHostId(), e);
            throw new ServiceException(BaseCode.SYSTEM_FAILED, e);
        } catch (Exception e) {
            throw new ServiceException(BaseCode.SYSTEM_FAILED, e);
        }
    }

    /**
     * 文件格式校验
     *
     * @param fileName 文件名称
     */
    private static void suffixVerify(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            throw new ServiceException("文件缺少后缀");
        }
        String fileSuffix = fileName.substring(index);
        List<String> list = Arrays.asList(".png", ".jpg", ".jpeg", ".PNG", ".JPEG", ".gif", ".pdf");
        if (!list.contains(fileSuffix)) {
            throw new ServiceException("暂不支持上传格式!");
        }
    }

    /**
     * 文件名_日期_随机字符串.后缀
     *
     * @param folder   文件夹名
     * @param fileName 文件名
     * @return 完整文件名
     */
    private static String genOssFileName(String folder, String fileName) {
        if (StringUtils.isBlank(folder) || Objects.equals(folder, "/")) {
            folder = "";
        } else {
            if (folder.startsWith("/")) {
                folder = folder.substring(1);
            }
            if (!folder.endsWith("/")) {
                folder = folder + "/";
            }
        }
        int index = fileName.lastIndexOf('.');
        String suffix = fileName.substring(index);
        if (index > 20) {
            index = 20;
        }
        String name = fileName.substring(0, index);
        return String.format("%s%s_%s_%s%s", folder, name, formatter.format(LocalDate.now()), RandomStringUtils.randomAlphanumeric(8), suffix);
    }
}
