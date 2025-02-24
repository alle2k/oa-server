package com.oa.core.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@ApiModel("业务详情视图")
@Data
public class BizDetailVo<T> {

    @ApiModelProperty("业务信息")
    private T bizInfo;
    /**
     * @see com.oa.core.enums.ApprovalSubmissionRecordStatusEnum
     */
    @ApiModelProperty("节点信息")
    private List<AuditNodeRecordVO> nodeInfo;

    public BizDetailVo(T bizInfo) {
        this.bizInfo = bizInfo;
    }
}
