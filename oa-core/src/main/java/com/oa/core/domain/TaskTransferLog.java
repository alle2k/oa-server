package com.oa.core.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @auther CodeGenerator
 * @create 2025-02-14 16:04:44
 * @describe 审批任务转交历史记录实体类
 */
@Data
@TableName("task_transfer_log")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value="TaskTransferLog对象", description="审批任务转交历史记录")
public class TaskTransferLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "PK")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "流程实例id")
    @TableField("instance_id")
    private String instanceId;

    @ApiModelProperty(value = "任务id")
    @TableField("task_id")
    private String taskId;

    @ApiModelProperty(value = "操作类型 1-转交 2-退回 3-撤回")
    @TableField("operation_type")
    private Integer operationType;

    @ApiModelProperty(value = "原处理人Id")
    @TableField("original_assignee")
    private Long originalAssignee;

    @ApiModelProperty(value = "被转交人id")
    @TableField("target_assignee")
    private Long targetAssignee;

    @ApiModelProperty(value = "评论信息")
    @TableField("review_data")
    private String reviewData;

    @ApiModelProperty(value = "删除标识（0-未删除，1-已删除）")
    @TableField("deleted")
    private Integer deleted;

    @ApiModelProperty(value = "创建人")
    @TableField("create_user")
    private Long createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "修改人")
    @TableField("update_user")
    private Long updateUser;

    @ApiModelProperty(value = "修改时间")
    @TableField("update_time")
    private Date updateTime;

}