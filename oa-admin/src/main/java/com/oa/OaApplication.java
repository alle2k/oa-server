package com.oa;

import org.flowable.spring.boot.FlowableSecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, FlowableSecurityAutoConfiguration.class})
public class OaApplication {
    public static void main(String[] args) {
        SpringApplication.run(OaApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  OA启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}
