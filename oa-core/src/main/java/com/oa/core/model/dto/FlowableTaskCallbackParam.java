package com.oa.core.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("任务转交")
@Data
public class FlowableTaskCallbackParam {

    @NotNull
    @ApiModelProperty(value = "Id")
    private Long id;

    /**
     * @see com.oa.core.enums.FlowableTaskCallbackOperateTypeEnum
     */
    @NotNull
    @ApiModelProperty("操作类型：0-退回，1-撤销")
    private Integer operateType;

    /**
     * @see com.oa.core.enums.AuditTypeEnum
     */
    @NotNull
    @ApiModelProperty("业务菜单标识")
    private Integer auditType;

    @ApiModelProperty("审批意见")
    private FlowableCommentParam comment;
}
