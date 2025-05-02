package com.oa.core.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class BusinessOrderSaveDto {

    /**
     * 付款时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    private Date paymentTime;

    /**
     * 甲方公司名称
     */
    @NotBlank
    @Size(min = 8, max = 20)
    private String companyName;

    /**
     * 甲方联系人姓名
     */
    @NotBlank
    private String companyContactUserName;

    /**
     * 甲方联系人电话
     */
    @NotBlank
    private String companyContactUserTel;

    /**
     * 成交金额
     */
    @NotNull
    @Min(0)
    private BigDecimal amount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 附件
     */
    private List<String> annexUrlList;

    /**
     * 打款截图
     */
    private List<String> paymentScreenshotList;

    /**
     * 业务类型
     *
     * @see com.oa.core.enums.BusinessOrderItemBizTypeEnum
     */
    @NotEmpty
    private List<Integer> bizTypeList;

    /**
     * 关联合同ID
     */
    private List<Long> refIds;
}
