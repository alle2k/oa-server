package com.oa.core.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("业务详情视图")
@Data
public class BizDetailVo<T extends BizInfoBaseVo> {

    @ApiModelProperty("审批编号")
    private String auditNo;
    @ApiModelProperty("抬头")
    private String title;
    @ApiModelProperty("创建人")
    private Long createUserId;
    @ApiModelProperty("创建人姓名")
    private String createUserName;
    @ApiModelProperty("创建人头像")
    private String avatar;
    @ApiModelProperty("当前审批用户列表")
    private List<UserShortVo> currentAuditUserList;
    @ApiModelProperty("业务信息")
    private T bizInfo;
    /**
     * @see com.oa.core.enums.ApprovalSubmissionRecordStatusEnum
     */
    @ApiModelProperty("审批状态 0-审批中 1-审批通过 2-已拒绝 4-已撤销 5-已退回")
    private Integer approvalStatus;
    @ApiModelProperty("节点信息")
    private List<AuditNodeRecordVO> nodeInfo;
}
