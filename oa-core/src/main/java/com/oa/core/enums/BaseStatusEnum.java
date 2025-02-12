package com.oa.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BaseStatusEnum {
    ENABLE(1, "有效"),
    DISABLE(0, "无效"),
    ;

    private final Integer code;
    private final String msg;
}
