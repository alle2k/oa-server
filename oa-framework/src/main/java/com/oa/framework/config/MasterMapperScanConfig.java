package com.oa.framework.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.oa.**.mapper.master", sqlSessionTemplateRef = "masterSqlSessionTemplate")
public class MasterMapperScanConfig {
}
