package com.oa.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @auther CodeGenerator
 * @create 2024-04-19 14:10:22
 * @describe 审批提交记录实体类
 */
@Data
@Builder
@TableName("approval_submission_record")
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalSubmissionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审批编号
     */
    @TableField("audit_no")
    private String auditNo;

    /**
     * 审批类型
     *
     * @see com.oa.core.enums.AuditTypeEnum
     */
    @TableField("audit_type")
    private Integer auditType;

    /**
     * 业务id
     */
    @TableField("biz_id")
    private Long bizId;

    /**
     * 申请人ID
     */
    @TableField("apply_user_id")
    private Long applyUserId;

    /**
     * 申请人部门
     */
    @TableField("apply_user_dept_id")
    private String applyUserDeptId;

    /**
     * 申请时间
     */
    @TableField("apply_time")
    private Date applyTime;

    /**
     * 审批流实例id
     */
    @TableField("instance_id")
    private String instanceId;

    /**
     * 审批状态 0-审批中 1-审批通过 2-已拒绝 4-已撤销 5-已退回
     *
     * @see com.oa.core.enums.ApprovalSubmissionRecordStatusEnum
     */
    @TableField("approval_status")
    private Integer approvalStatus;

    /**
     * 审批完成时间
     */
    @TableField("approval_time")
    private Date approvalTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 删除标识 0-未删除 1-已删除
     */
    @TableField("deleted")
    private Integer deleted;

    /**
     * 创建人
     */
    @TableField("create_user")
    private Long createUser;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 修改人
     */
    @TableField("update_user")
    private Long updateUser;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private Date updateTime;
}
