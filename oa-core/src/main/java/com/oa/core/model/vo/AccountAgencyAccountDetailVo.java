package com.oa.core.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class AccountAgencyAccountDetailVo {

    @ApiModelProperty(value = "PK")
    private Long id;

    @ApiModelProperty(value = "合同订单ID")
    private Long orderId;

    @ApiModelProperty(value = "合同编号")
    private String contractNo;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "服务开始时间")
    private Date serviceBeginDate;

    @ApiModelProperty(value = "服务结束时间")
    private Date serviceEndDate;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "甲方公司名称")
    private String companyName;

    @ApiModelProperty(value = "甲方联系人姓名")
    private String companyContactUserName;

    @ApiModelProperty(value = "甲方联系人电话")
    private String companyContactUserTel;

    @ApiModelProperty(value = "成交金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "付款时间")
    private Date paymentTime;

    private String annexUrl;

    private String paymentScreenshot;

    private List<String> annexUrlList;

    /**
     * 打款截图
     */
    private List<String> paymentScreenshotList;

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 用户姓名
     */
    private String createUserName;
}
