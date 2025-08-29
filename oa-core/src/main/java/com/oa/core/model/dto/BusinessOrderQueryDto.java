package com.oa.core.model.dto;

import com.oa.common.core.domain.model.DataPermissionDto;
import com.oa.common.core.page.PageDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessOrderQueryDto extends PageDomain {

    /**
     * 审批编号
     */
    private String auditNo;

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 审批状态 0-审批中 1-审批通过 2-已拒绝 4-已撤销 5-已退回
     *
     * @see com.oa.core.enums.ApprovalSubmissionRecordStatusEnum
     */
    private Integer approvalStatus;

    /**
     * 甲方公司名称
     */
    private String companyName;

    private DataPermissionDto dataPermission;

    /**
     * 业务类型
     *
     * @see com.oa.core.enums.BusinessOrderItemBizTypeEnum
     */
    private Integer bizType;

    private Integer menuFlag;

    /**
     * 业务订单ID列表
     */
    private Collection<Long> ids;
}
