package com.oa.core.model.vo;

import lombok.Data;

@Data
public class BizDetailBaseVo {

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 用户姓名
     */
    private String createUserName;

    private Long createUserDeptId;

    private String createUserDeptName;

    private String createUserFullDeptId;

    private String createUserFullDeptName;
}
