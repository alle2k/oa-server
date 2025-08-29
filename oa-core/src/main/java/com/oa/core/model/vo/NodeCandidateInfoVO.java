package com.oa.core.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @auther 麻油叶
 * @create 2024/4/22 18:35
 * @describe
 */
@Data
public class NodeCandidateInfoVO {

    /**
     * @see com.oa.flowable.enums.CandidateTypeEnum
     */
    @ApiModelProperty("候选类型：0-审批，1-抄送")
    private Integer candidateType;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户头像")
    private String userPhoto;

    @ApiModelProperty("部门id")
    private Long departmentId;

    @ApiModelProperty("部门名称")
    private String departmentName;

    @ApiModelProperty("用户名称")
    private String userName;
}
