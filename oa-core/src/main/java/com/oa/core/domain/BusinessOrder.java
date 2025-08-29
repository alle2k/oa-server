package com.oa.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@NoArgsConstructor
@Data
@TableName("business_order")
public class BusinessOrder {

    /**
     * PK
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审批编号
     */
    @TableField("audit_no")
    private String auditNo;

    /**
     * 部门ID
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 付款时间
     */
    @TableField("payment_time")
    private Date paymentTime;

    /**
     * 甲方公司名称
     */
    @TableField("company_name")
    private String companyName;

    /**
     * 甲方联系人姓名
     */
    @TableField("company_contact_user_name")
    private String companyContactUserName;

    /**
     * 甲方联系人电话
     */
    @TableField("company_contact_user_tel")
    private String companyContactUserTel;

    /**
     * 成交金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 已使用金额
     */
    @TableField("used_amount")
    private BigDecimal usedAmount;

    /**
     * 剩余可用金额
     */
    @TableField("free_amount")
    private BigDecimal freeAmount;

    /**
     * 业绩
     */
    @TableField("performance")
    private BigDecimal performance;

    /**
     * 附件
     */
    @TableField("annex_url")
    private String annexUrl;

    /**
     * 打款截图
     */
    @TableField("payment_screenshot")
    private String paymentScreenshot;

    /**
     * 审批状态 0-审批中 1-审批通过 2-已拒绝 4-已撤销 5-已退回
     *
     * @see com.oa.core.enums.ApprovalSubmissionRecordStatusEnum
     */
    @TableField("approval_status")
    private Integer approvalStatus;

    /**
     * 审批通过时间
     */
    @TableField("approval_time")
    private Date approvalTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 删除状态 0-未删除 1-已删除
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

    public BusinessOrder(String auditNo, String companyName, String companyContactUserName, String companyContactUserTel, BigDecimal amount, BigDecimal usedAmount, BigDecimal freeAmount,Integer approvalStatus, Date approvalTime) {
        this.auditNo = auditNo;
        this.companyName = companyName;
        this.companyContactUserName = companyContactUserName;
        this.companyContactUserTel = companyContactUserTel;
        this.amount = amount;
        this.usedAmount = usedAmount;
        this.freeAmount = freeAmount;
        this.approvalStatus = approvalStatus;
        this.approvalTime = approvalTime;
    }
}
