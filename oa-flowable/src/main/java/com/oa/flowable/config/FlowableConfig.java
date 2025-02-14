package com.oa.flowable.config;

import com.oa.flowable.listener.ProcessInstanceStatusListener;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RuntimeService;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration
public class FlowableConfig {

    @Resource
    private ProcessInstanceStatusListener processInstanceStatusListener;

    @Bean
    public ProcessEngine processEngine(DataSourceTransactionManager transactionManager,
                                       DataSource dataSource) {
        SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
        configuration.setTransactionManager(transactionManager);
        // 执行工作流对应的数据源
        configuration.setDataSource(dataSource);
        // 是否自动创建流程引擎表
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        // 流程图字体
        configuration.setActivityFontName("宋体");
        configuration.setAnnotationFontName("宋体");
        configuration.setLabelFontName("宋体");
        return configuration.buildProcessEngine();
    }

    @Bean("runtimeService")
    public RuntimeService runtimeService(@Qualifier("processEngine") ProcessEngine processEngine) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        runtimeService.addEventListener(processInstanceStatusListener);
        return runtimeService;
    }
}
