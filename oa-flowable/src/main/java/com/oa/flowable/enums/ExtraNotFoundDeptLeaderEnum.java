package com.oa.flowable.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum ExtraNotFoundDeptLeaderEnum {

    RECURSIVE_UP_DEPT_LEADER(0, "由上级负责人待审批"),
    PASS(1, "直接审批通过"),
    ADMIN_AUDIT(2, "自动转交管理员"),
    ;

    public static final Map<Integer, ExtraNotFoundDeptLeaderEnum> codeMap;
    private final Integer value;
    private final String desc;

    static {
        codeMap = Arrays.stream(values()).collect(Collectors.toMap(ExtraNotFoundDeptLeaderEnum::getValue, Function.identity()));
    }
}
