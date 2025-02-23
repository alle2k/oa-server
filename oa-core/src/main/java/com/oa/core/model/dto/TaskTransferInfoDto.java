package com.oa.core.model.dto;

import lombok.Data;

import java.util.Date;

/**
 * @auther 麻油叶
 * @create 2024/4/24 15:42
 * @describe
 */
@Data
public class TaskTransferInfoDto {
    /**
     * 原处理人
     */
    private Long originalAssignee;

    private String originalAssigneeName;

    /**
     * 被转交人
     */
    private Long targetAssignee;

    private String targetAssigneeName;

    /**
     * 转交时间
     */
    private Date transferTime;
}
