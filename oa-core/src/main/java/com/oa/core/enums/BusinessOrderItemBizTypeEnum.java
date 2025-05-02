package com.oa.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum BusinessOrderItemBizTypeEnum {

    BUSINESS_REGISTRATION_AGENCY(0, "工商代办"),
    ACCOUNT_AGENCY(1, "代理记账"),
    COMPANY_DE_REGISTRATION(2, "公司注销"),
    INTELLECTUAL_PROPERTY(3, "知识产权"),
    PROJECT_APPLICATION(4, "项目申报"),
    OTHERS(5, "其他"),
    ACCOUNT_AGENCY_EXTEND(6, "代理记账续期"),
    ;

    public static final Map<Integer, BusinessOrderItemBizTypeEnum> codeMap;
    private final int code;
    private final String desc;

    static {
        codeMap = Arrays.stream(values()).collect(Collectors.toMap(BusinessOrderItemBizTypeEnum::getCode, Function.identity()));
    }
}
