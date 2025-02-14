package com.oa.core.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("任务转交")
@Data
public class FlowableTaskTransferParam {

    @NotNull
    @ApiModelProperty(value = "Id")
    private Long id;

    @NotNull
    @ApiModelProperty("目标用户ID")
    private Long targetUserId;

    /**
     * @see com.oa.core.enums.AuditTypeEnum
     */
    @NotNull
    @ApiModelProperty("业务菜单标识：1001-立项申请，1002-发货申请，1003-退货申请，1004-开票申请")
    private Integer auditType;

    @ApiModelProperty("审批意见")
    private FlowableCommentParam comment;
}
