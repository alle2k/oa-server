package com.oa.flowable.config;

import com.oa.flowable.listener.ProcessInstanceStatusListener;
import org.flowable.engine.*;
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

    @Bean("repositoryService")
    public RepositoryService repositoryService(@Qualifier("processEngine") ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    @Bean("runtimeService")
    public RuntimeService runtimeService(@Qualifier("processEngine") ProcessEngine processEngine) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        runtimeService.addEventListener(processInstanceStatusListener);
        return runtimeService;
    }

    @Bean("taskService")
    public TaskService taskService(@Qualifier("processEngine")ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    @Bean("historyService")
    public HistoryService historyService(@Qualifier("processEngine")ProcessEngine processEngine) {
        return processEngine.getHistoryService();
    }

    @Bean("managementService")
    public ManagementService managementService(@Qualifier("processEngine")ProcessEngine processEngine) {
        return processEngine.getManagementService();
    }

    @Bean("identityService")
    public IdentityService identityService(@Qualifier("processEngine")ProcessEngine processEngine) {
        return processEngine.getIdentityService();
    }
}
