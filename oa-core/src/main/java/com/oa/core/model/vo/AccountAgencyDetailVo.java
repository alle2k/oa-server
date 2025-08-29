package com.oa.core.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AccountAgencyDetailVo extends BizDetailBaseVo {

    @ApiModelProperty(value = "PK")
    private Long id;

    @ApiModelProperty(value = "审批编号")
    private String auditNo;

    @ApiModelProperty(value = "合同订单ID")
    private Long orderId;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "服务开始时间")
    private Date serviceBeginDate;

    @ApiModelProperty(value = "服务结束时间")
    private Date serviceEndDate;

    @ApiModelProperty(value = "审批状态 0-审批中 1-审批通过 2-已拒绝 4-已撤销 5-已退回")
    private Integer approvalStatus;

    @ApiModelProperty(value = "审批通过时间")
    private Date approvalTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "审批编号")
    private String orderAuditNo;

    @ApiModelProperty(value = "付款时间")
    private Date paymentTime;

    @ApiModelProperty(value = "甲方公司名称")
    private String companyName;

    @ApiModelProperty(value = "甲方联系人姓名")
    private String companyContactUserName;

    @ApiModelProperty(value = "甲方联系人电话")
    private String companyContactUserTel;

    @ApiModelProperty(value = "成交金额")
    private BigDecimal orderAmount;

    /**
     * 已使用金额
     */
    private BigDecimal usedAmount;

    /**
     * 剩余可用金额
     */
    private BigDecimal freeAmount;


    private List<String> annexUrlList;

    /**
     * 打款截图
     */
    private List<String> paymentScreenshotList;
}
