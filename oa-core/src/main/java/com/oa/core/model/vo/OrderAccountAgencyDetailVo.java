package com.oa.core.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderAccountAgencyDetailVo {

    private Long bizId;

    private Integer bizType;

    private Long orderId;

    private Date createTime;

    private Date approvalTime;

    private String avatar;

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 用户姓名
     */
    private String createUserName;

    private BigDecimal amount;

    private String desc;
}
