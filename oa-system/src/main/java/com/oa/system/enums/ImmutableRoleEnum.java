package com.oa.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ImmutableRoleEnum {

    ADMIN(1, "系统管理员", "admin"),
    BOSS(2, "总经办", "boss"),
    ACCOUNTANT(3, "财务专员", "accountant"),
    ACCOUNTANT_MANAGER(4, "财务主管", "accountantManager"),
    ;

    private final int code;
    private final String key;
    private final String desc;
}
