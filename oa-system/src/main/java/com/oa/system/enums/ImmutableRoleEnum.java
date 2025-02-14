package com.oa.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ImmutableRoleEnum {

    ADMIN(1, "系统管理员", "admin"),
    BOSS(2, "总经办", "boss"),
    ACCOUNTANT(3, "财务", "accountant"),
    ;

    private final int code;
    private final String key;
    private final String desc;
}
