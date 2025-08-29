package com.oa;

import cn.hutool.core.util.XmlUtil;
import com.alibaba.fastjson2.JSON;
import com.oa.core.service.impl.ApprovalSubmissionRecordServiceImpl;
import com.oa.flowable.constants.FlowableConstants;
import com.oa.flowable.enums.CandidateTypeEnum;
import com.oa.flowable.enums.ProcessStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.spring.impl.test.FlowableSpringExtension;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@ExtendWith(FlowableSpringExtension.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OaApplication.class)
@ActiveProfiles(value = "test")
public class ProcessUtilsTest {
    @Resource
    RuntimeService runtimeService;
    @Resource
    TaskService taskService;
    @Resource
    RepositoryService repositoryService;

    @Test
    void createProcess() {
        // 启动流程实例
        Map<String, Object> variables = new HashMap<>();
        variables.put("employee", "小明");
        variables.put("validateForm", true);
        variables.put("manager", "张三");
        variables.put("approve", "true");
        variables.put("hr", "李四");
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("leaveProcess", variables);
//        runtimeService.addEventListener(processInstanceStatusListener);
        log.info("流程实例ID:{}---流程定义ID:{}", pi.getId(), pi.getProcessDefinitionId());
        Task task = null;
        while (pi != null && !pi.isEnded()) {
            // 查询当前任务
            task = taskService.createTaskQuery()
                    .processInstanceId(pi.getId())
                    .singleResult();
            // 完成当前任务
            taskService.complete(task.getId());
            // 查询流程实例状态
            pi = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(pi.getId())
                    .singleResult();
        }
        System.out.println("任务已完成");
    }

    @Test
    public void deployFlow() {
        try (InputStream fis = Files.newInputStream(Paths.get("C:\\Users\\battl\\Desktop\\bpmn20.xml"))) {
            Deployment deploy = repositoryService.createDeployment()
                    .addInputStream("bpmn20.xml", fis)
                    .name("退货申请DEMO")
                    .key("shippedDeviceReturn")
                    .deploy();
            log.info("部署成功:{}", deploy.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeployQuery() {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId("1").singleResult();
        log.info("{}", processDefinition);
        System.out.println(processDefinition.getId());
        System.out.println(processDefinition.getName());
        System.out.println(processDefinition.getResourceName());
        System.out.println(processDefinition.getDeploymentId());
        System.out.println(processDefinition.getDescription());
    }

    @Test
    public void testRunProcess() {
        Authentication.setAuthenticatedUserId("11");
        Map<String, Object> variables = new HashMap<>();
        variables.put(FlowableConstants.AUDIT_VAR_NAME, ProcessStatusEnum.COMPLETE);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("shippedDeviceReturn", variables);
        System.out.println(processInstance.getProcessDefinitionId());
        System.out.println(processInstance.getId());
        System.out.println(processInstance.getActivityId());
    }

    @Test
    public void testQueryTask() {
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("projectApply").taskCandidateOrAssigned("{userId:1}").singleResult();
        Map<String, Object> maps = new HashMap<>();
        maps.put("employee", "zhangsan");
        maps.put("nrOfHolidays", 3);
        taskService.complete(task.getId(), maps);
        /*List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey("holidayRequest").taskCandidateGroup("managers").list();
        list.forEach(task -> {
            System.out.println("task.getProcessDefinitionId() = " + task.getProcessDefinitionId());
            System.out.println("task.getId() = " + task.getId());
            System.out.println("task.getAssignee() = " + task.getAssignee());
            System.out.println("task.getName() = " + task.getName());
        });*/
    }

    @Test
    public void getBpmnModel() {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("projectApply")
                .latestVersion()
                .singleResult();
        System.out.println(processDefinition.getId());
        System.out.println(processDefinition.getDeploymentId());
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();
        Iterator<FlowElement> iterator = flowElements.iterator();
        do {
            FlowElement next = iterator.next();

        } while (iterator.hasNext());
        System.out.println("1");
    }

    @Test
    public void testDeleteProcess() {
        repositoryService.deleteDeployment("1", true);
    }

    @Test
    public void buildBpmnModel() {
        Process process = new Process();
        process.setId("test");
        process.setCandidateStarterUsers(Collections.singletonList("张三"));
        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent");
        startEvent.setName("开始事件");
        process.addFlowElement(startEvent);
        BpmnModel model = new BpmnModel();
        UserTask userTask = new UserTask();
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", 1);
        map.put("name", "张三");
        HashMap<String, Object> map2 = new HashMap<>();
        map2.put("userId", 2);
        map2.put("name", "里斯");
        List<String> list = new LinkedList<>();
        list.add(JSON.toJSONString(map));
        list.add(JSON.toJSONString(map2));
        userTask.setCandidateUsers(list);
        process.addFlowElement(userTask);
        SequenceFlow agreeFlow = new SequenceFlow();
        agreeFlow.setSourceRef(startEvent.getId());
        agreeFlow.setTargetRef(userTask.getId());
        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent");
        endEvent.setName("结束事件");
        SequenceFlow applyFlow = new SequenceFlow();
        applyFlow.setSourceRef(userTask.getId());
        applyFlow.setTargetRef(endEvent.getId());
        process.addFlowElement(endEvent);
        model.addProcess(process);

        BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        byte[] bpmnBytes = xmlConverter.convertToXML(model);
        String str = new String(bpmnBytes);
        System.out.println(str);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("selectType", 1);
        map.put("selectItem", "12");
        map.put("type", CandidateTypeEnum.AUDIT.getValue());
        System.out.println(XmlUtil.mapToXmlStr(map));
    }

    @Autowired
    ApprovalSubmissionRecordServiceImpl approvalSubmissionRecordService;
    @Autowired
    HistoryService historyService;

    @Test
    void clear() {
        // 删除运行中的流程实例
//        runtimeService.deleteProcessInstance("2501", "删除原因");
        // 删除历史数据
        historyService.deleteHistoricProcessInstance("20001");
        historyService.deleteHistoricProcessInstance("27501");
        historyService.deleteHistoricProcessInstance("30001");
        historyService.deleteHistoricProcessInstance("32501");
        historyService.deleteHistoricProcessInstance("37501");
        historyService.deleteHistoricProcessInstance("35001");
    }

    @Test
    void addCandidateUser() {
//        taskService.addCandidateUser("607526", "赵恺");
//        taskService.claim("607526", "赵恺");
        taskService.unclaim("607526");
        taskService.deleteCandidateUser("607526", "赵恺");
    }
}
