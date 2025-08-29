package com.oa.common.config;

import com.oa.common.config.properties.ObsProperties;
import com.obs.services.ObsClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ObsProperties.class})
public class ObsClientConfig {

    @Bean
    public ObsClient obsClient(ObsProperties obsProperties) {
        return new ObsClient(obsProperties.getAccessKeyId(), obsProperties.getSecretAccessKeyId(), obsProperties.getEndPoint());
    }
}
