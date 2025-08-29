package com.oa.core.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@ApiModel("审批意见")
@Data
public class FlowableCommentParam {

    @ApiModelProperty(value = "备注")
    private String remark;
    @ApiModelProperty(value = "附件")
    private List<String> annexUrl;
    @ApiModelProperty(value = "转交的用户ID", hidden = true)
    private String transferTo;

    public FlowableCommentParam(String remark) {
        this.remark = remark;
    }
}
