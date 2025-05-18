package com.oa.core.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class BusinessOrderDetailVo {

    /**
     * PK
     */
    private Long id;

    /**
     * 审批编号
     */
    private String auditNo;

    /**
     * 付款时间
     */
    private Date paymentTime;

    /**
     * 甲方公司名称
     */
    private String companyName;

    /**
     * 甲方联系人姓名
     */
    private String companyContactUserName;

    /**
     * 甲方联系人电话
     */
    private String companyContactUserTel;

    /**
     * 成交金额
     */
    private BigDecimal amount;

    /**
     * 业绩
     */
    private BigDecimal performance;

    /**
     * 附件
     */
    private String annexUrl;

    /**
     * 附件列表
     */
    private List<String> annexUrlList;

    /**
     * 打款截图
     */
    private String paymentScreenshot;

    /**
     * 打款截图列表
     */
    private List<String> paymentScreenshotList;

    /**
     * 审批状态 0-审批中 1-审批通过 2-已拒绝 4-已撤销 5-已退回
     *
     * @see com.oa.core.enums.ApprovalSubmissionRecordStatusEnum
     */
    private Integer approvalStatus;

    /**
     * 审批通过时间
     */
    private Date approvalTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 用户姓名
     */
    private String createUserName;

    private Long createUserDeptId;

    private String createUserDeptName;

    private String createUserFullDeptId;

    private String createUserFullDeptName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 服务内容列表
     */
    private List<BusinessOrderItemDetailVo> itemList;

    /**
     * 关联合同列表
     */
    private List<BusinessOrderShortVo> refOrderList;
}
