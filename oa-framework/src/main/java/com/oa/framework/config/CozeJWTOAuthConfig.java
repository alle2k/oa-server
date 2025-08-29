package com.oa.framework.config;

import com.coze.openapi.service.auth.JWTOAuthClient;
import com.coze.openapi.service.config.Consts;
import com.oa.common.error.BaseCode;
import com.oa.common.exception.ServiceException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "coze")
public class CozeJWTOAuthConfig {

    private String clientId;
    private String privateFilePath;
    private String publicKey;

    @Bean
    public JWTOAuthClient jwtOAuthClient() {
        JWTOAuthClient oauth;
        String privateKey;
        try {
            privateKey =
                    new String(
                            Files.readAllBytes(Paths.get(privateFilePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取私钥文件失败", e);
            throw new ServiceException(BaseCode.SYSTEM_FAILED);
        }

        /*
            The jwt oauth type requires using private to be able to issue a jwt token, and through
            the jwt token, apply for an access_token from the coze service. The sdk encapsulates
            this procedure, and only needs to use get_access_token to obtain the access_token under
            the jwt oauth process.
            Generate the authorization token
            The default ttl is 900s, and developers can customize the expiration time, which can be
            set up to 24 hours at most.
        * */
        try {
            oauth =
                    new JWTOAuthClient.JWTOAuthBuilder()
                            .clientID(clientId)
                            .privateKey(privateKey)
                            .publicKey(publicKey)
                            .baseURL(Consts.COZE_CN_BASE_URL)
                            .ttl(24 * 60 * 60)
                            .build();
        } catch (Exception e) {
            log.error("创建JWTOauthClient失败", e);
            throw new ServiceException(BaseCode.SYSTEM_FAILED);
        }
        return oauth;
    }
}
