package com.oa.core.model.dto;

import com.oa.common.core.domain.model.DataPermissionDto;
import com.oa.common.core.page.PageDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AccountAgencyQueryDto extends PageDomain {

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
     * 审批状态列表
     */
    private List<Integer> approvalStatusList;

    /**
     * 数据权限信息
     */
    private DataPermissionDto dataPermission;

    /**
     * 逾期标识
     */
    private Integer overdueFlag;
}
