package com.oa.core.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessOrderPageQueryVo extends BusinessOrderShortVo {

    /**
     * 用户姓名
     */
    private String createUserName;
}
