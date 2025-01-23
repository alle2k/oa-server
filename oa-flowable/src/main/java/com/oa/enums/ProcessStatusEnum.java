package com.oa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProcessStatusEnum {
    AUDIT("audit", "审核中"),
    RECALL("recall", "已撤回"),
    COMPLETE("agree", "已通过"),
    REJECT("reject", "已拒绝"),
    ;

    private final String code;
    private final String msg;
}
