package com.oa.core.model.dto;

import com.oa.core.enums.AuditTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("保存审批提交记录")
@Data
public class ApprovalSubmissionRecordSaveDto {

    @ApiModelProperty(value = "审批流实例id")
    private String instanceId;
    @ApiModelProperty(value = "业务id")
    private Long bizId;
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("审批流类型")
    private AuditTypeEnum auditTypeEnum;
}
