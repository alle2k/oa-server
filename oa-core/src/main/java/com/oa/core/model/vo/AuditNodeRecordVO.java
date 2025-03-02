package com.oa.core.model.vo;

import com.oa.core.model.dto.TaskTransferInfoDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @auther 麻油叶
 * @create 2024/4/22 9:57
 * @describe
 */
@Data
public class AuditNodeRecordVO {
    @ApiModelProperty("节点类型 1-开始节点 2-用户任务节点 3-任务转交节点 4-任务退回节点 5-任务撤回节点,6-抄送节点，7-重新提交节点")
    private Integer nodeType;

    @ApiModelProperty("审批状态 1-审批通过 2-审批拒接 3-待审批 ")
    private Integer auditStatus;

    @ApiModelProperty("候选人信息")
    private List<NodeCandidateInfoVO> nodeCandidateInfo;

    @ApiModelProperty("评论信息")
    private String commentInfo;

    private String nodeName;

    @ApiModelProperty("备注信息")
    private String noteInformation;

    @ApiModelProperty("评论附件url")
    private List<String> commentAttachmentsUrl;

    @ApiModelProperty("任务转交信息")
    private TaskTransferInfoDto taskTransferInfoDto;

    @ApiModelProperty("发起时间")
    private Date startTime;

    private Date nodeTime;

    private String taskKey;
}
