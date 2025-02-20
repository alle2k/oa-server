package com.oa.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "huawei.obs")
public class ObsProperties {

    private String accessKeyId;
    private String secretAccessKeyId;
    private String endPoint;
    private String bucketName;
}
