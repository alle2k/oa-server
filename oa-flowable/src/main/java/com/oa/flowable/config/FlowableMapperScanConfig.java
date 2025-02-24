package com.oa.flowable.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.oa.**.mapper.flowable", sqlSessionTemplateRef = "flowableSqlSessionTemplate", sqlSessionFactoryRef = "flowableSqlSessionFactory")
public class FlowableMapperScanConfig {
}
