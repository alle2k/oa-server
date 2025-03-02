package com.oa.core.enums;

import com.oa.common.utils.spring.SpringUtils;
import com.oa.core.processor.AbstractAuditBizProcessor;
import com.oa.core.processor.BusinessOrderAuditProcessor;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum AuditTypeEnum {

    APPROVAL_BUSINESS_ORDER(1001, "businessOrder", "提交订单申请", BusinessOrderAuditProcessor.class),
    ;

    public final static Map<Integer, AuditTypeEnum> codeMap;
    private final Integer code;
    private final String processDefinitionKey;
    private final String desc;
    /**
     * spring容器中的bean名, 实现 IAuditProcessor
     */
    private final Class<? extends AbstractAuditBizProcessor> processor;

    public static AbstractAuditBizProcessor getProcessorBean(Integer code) {
        return SpringUtils.getBean(codeMap.get(code).getProcessor());
    }

    public AbstractAuditBizProcessor getProcessorBean() {
        return SpringUtils.getBean(this.processor);
    }

    static {
        codeMap = Arrays.stream(values()).collect(Collectors.toMap(AuditTypeEnum::getCode, Function.identity()));
    }
}
