package com.oa.core.config;

import com.oa.core.model.dto.AuditFormBusinessOrderDto;

import java.util.HashMap;
import java.util.Map;

public class FlowableSpecialApprovalConfig {

    public static final Map<String, Class<?>> SPECIAL_APPROVAL_FORM_MAP;

    static {
        SPECIAL_APPROVAL_FORM_MAP = new HashMap<>();
        SPECIAL_APPROVAL_FORM_MAP.put("businessOrderUserTask1", AuditFormBusinessOrderDto.class);
    }
}
