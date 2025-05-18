package com.oa.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DeletedEnum {

    DELETED(1, "已删除"),
    UN_DELETE(0, "未删除"),
    ;

    private final Integer code;
    private final String msg;
}
