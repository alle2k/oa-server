package com.oa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CandidateTypeEnum {

    AUDIT(0, "审批"),
    CC(1, "抄送"),
    ;

    private final Integer value;
    private final String desc;
}
