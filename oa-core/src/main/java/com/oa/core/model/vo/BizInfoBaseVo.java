package com.oa.core.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@ApiModel("业务信息")
@Data
public class BizInfoBaseVo {

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("附件列表")
    private List<String> annexList;

    public BizInfoBaseVo(String remark, List<String> annexList) {
        this.remark = remark;
        this.annexList = annexList;
    }
}
