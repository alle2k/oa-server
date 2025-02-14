package com.oa.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FlowableTaskCallbackOperateTypeEnum {

    CALLBACK(0, "退回"),
    CANCEL(1, "撤销"),
    ;

    private final Integer value;
    private final String desc;
}
