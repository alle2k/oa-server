package com.oa.flowable.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum CandidateGroupsSelectTypeEnum {

    DEPT_SUPERVISOR(0, "直属主管"),
    ROLE(1, "指定角色"),
    DEPT_MANAGER(2, "部门负责人"),
    ;

    public static final Map<Integer, CandidateGroupsSelectTypeEnum> codeMap;
    private final Integer value;
    private final String desc;

    static {
        codeMap = Arrays.stream(values()).collect(Collectors.toMap(CandidateGroupsSelectTypeEnum::getValue, Function.identity()));
    }
}
