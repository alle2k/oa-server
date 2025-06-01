package com.oa.core.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.oa.common.core.page.PageDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class AccountAgencyAccountQueryDto extends PageDomain {

    private String contractNo;

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 服务开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date serviceBeginDate;

    /**
     * 服务结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date serviceEndDate;
}
