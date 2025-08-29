package com.oa.core.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BusinessOrderItemDetailVo {

    @ApiModelProperty(value = "PK")
    private Long id;

    @ApiModelProperty("合同ID")
    private Long orderId;

    @ApiModelProperty(value = "业务类型")
    private Integer bizType;

    @ApiModelProperty("业务类型名称")
    private String bizTypeName;
}
