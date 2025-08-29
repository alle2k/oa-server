package com.oa.flowable.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum ProcessStatusEnum {
    AUDIT("audit", "审核中"),
    RECALL("recall", "已撤回"),
    COMPLETE("agree", "已通过"),
    REJECT("reject", "已拒绝"),
    ;

    public final static Map<String, ProcessStatusEnum> codeMap;
    private final String code;
    private final String msg;

    static {
        codeMap = Arrays.stream(values()).collect(Collectors.toMap(ProcessStatusEnum::getCode, Function.identity()));
    }
}
