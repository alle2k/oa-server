package com.oa.core.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@ApiModel(value = "审批事件审批参数")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlowableAuditParam {

    @NotNull
    @ApiModelProperty(value = "Id")
    private Long id;

    /**
     * @see com.oa.core.enums.AuditTypeEnum
     */
    @NotNull
    @ApiModelProperty("业务菜单标识")
    private Integer auditType;

    @ApiModelProperty(value = "审核动作 agree:同意, reject:拒绝")
    @NotBlank(message = "审核动作不能为空")
    private String auditAction;

    @ApiModelProperty("审批意见")
    private FlowableCommentParam comment;

    @ApiModelProperty("额外信息，JSON格式")
    private String extra;
}