package com.oa.core.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("催办")
@Data
public class FlowableRemindParam {

    @NotNull
    @ApiModelProperty(value = "业务ID", required = true)
    private Long id;
    @NotNull
    @ApiModelProperty(value = "业务标识：1001-立项申请，1002-发货申请，1003-退货申请，1004-开票申请", required = true)
    private Integer auditType;
}
