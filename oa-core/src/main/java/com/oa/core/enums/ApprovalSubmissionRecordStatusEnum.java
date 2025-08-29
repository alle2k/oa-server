package com.oa.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApprovalSubmissionRecordStatusEnum {

    AUDIT(0, "审批中"),
    PASS(1, "审批通过"),
    REJECT(2, "已拒绝"),
    CANCEL(4, "已撤销"),
    ROLLBACK(5, "已退回"),
    ;
    private final int code;
    private final String desc;
}
