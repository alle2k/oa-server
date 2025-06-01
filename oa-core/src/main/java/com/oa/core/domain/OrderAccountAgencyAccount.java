package com.oa.core.domain;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @auther CodeGenerator
 * @create 2025-05-31 17:05:33
 * @describe 代理记账台账实体类
 */
@Data
@TableName("order_account_agency_account")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value="OrderAccountAgencyAccount对象", description="代理记账台账")
public class OrderAccountAgencyAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "PK")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "合同订单ID")
    @TableField("order_id")
    private Long orderId;

    @ApiModelProperty(value = "合同编号")
    @TableField("contract_no")
    private String contractNo;

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
    @TableField("order_amount")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "金额")
    @TableField("amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "服务开始时间")
    @TableField("service_begin_date")
    private Date serviceBeginDate;

    @ApiModelProperty(value = "服务结束时间")
    @TableField("service_end_date")
    private Date serviceEndDate;

    @ApiModelProperty(value = "税号")
    @TableField("tax_no")
    private String taxNo;

    @ApiModelProperty(value = "公司性质")
    @TableField("company_nature")
    private String companyNature;

    @ApiModelProperty(value = "是否有税控")
    @TableField("has_tax_ctrl")
    private String hasTaxCtrl;

    @ApiModelProperty(value = "身份证号码")
    @TableField("id_card")
    private String idCard;

    @ApiModelProperty(value = "密码")
    @TableField("password")
    private String password;

    @ApiModelProperty(value = "注册地税管员电话")
    @TableField("register_tax_manager_phone")
    private String registerTaxManagerPhone;

    @ApiModelProperty(value = "个税密码")
    @TableField("individual_income_tax_password")
    private String individualIncomeTaxPassword;

    @ApiModelProperty(value = "公司情况")
    @TableField("company_detail")
    private String companyDetail;

    @ApiModelProperty(value = "销售")
    @TableField("sale")
    private String sale;

    @ApiModelProperty(value = "付费周期")
    @TableField("pay_period")
    private String payPeriod;

    @ApiModelProperty(value = "附件")
    @TableField("annex_url")
    private String annexUrl;

    /**
     * 打款截图
     */
    @TableField("payment_screenshot")
    private String paymentScreenshot;

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