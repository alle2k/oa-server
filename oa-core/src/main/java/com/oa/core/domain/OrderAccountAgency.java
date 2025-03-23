package com.oa.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @auther CodeGenerator
 * @create 2025-03-12 19:05:13
 * @describe 代理记账实体类
 */
@Data
@TableName("order_account_agency")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value = "OrderAccountAgency对象", description = "代理记账")
public class OrderAccountAgency implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "PK")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "审批编号")
    @TableField("audit_no")
    private String auditNo;

    @ApiModelProperty(value = "合同订单ID")
    @TableField("order_id")
    private Long orderId;

    @ApiModelProperty(value = "金额")
    @TableField("amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "服务开始时间")
    @TableField("service_begin_date")
    private Date serviceBeginDate;

    @ApiModelProperty(value = "服务结束时间")
    @TableField("service_end_date")
    private Date serviceEndDate;

    @ApiModelProperty("税号")
    @TableField("tax_no")
    private String taxNo;
    @ApiModelProperty("公司性质")
    @TableField("company_nature")
    private String companyNature;
    @ApiModelProperty("是否有税控")
    @TableField("has_tax_ctrl")
    private String hasTaxCtrl;
    @ApiModelProperty("身份证号码")
    @TableField("id_card")
    private String idCard;
    @ApiModelProperty("密码")
    @TableField("password")
    private String password;
    @ApiModelProperty("注册地税管员电话")
    @TableField("register_tax_manager_phone")
    private String registerTaxManagerPhone;
    @ApiModelProperty("个税密码")
    @TableField("individual_income_tax_password")
    private String individualIncomeTaxPassword;
    @ApiModelProperty("公司情况")
    @TableField("company_detail")
    private String companyDetail;
    @ApiModelProperty("销售")
    @TableField("sale")
    private String sale;
    @ApiModelProperty("付费周期")
    @TableField("pay_period")
    private String payPeriod;

    @ApiModelProperty(value = "附件")
    @TableField("annex_url")
    private String annexUrl;

    @ApiModelProperty(value = "审批状态 0-审批中 1-审批通过 2-已拒绝 4-已撤销 5-已退回")
    @TableField("approval_status")
    private Integer approvalStatus;

    @ApiModelProperty(value = "审批通过时间")
    @TableField("approval_time")
    private Date approvalTime;

    @ApiModelProperty(value = "备注")
    @TableField("remark")
    private String remark;

    @ApiModelProperty(value = "删除状态 0-未删除 1-已删除")
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