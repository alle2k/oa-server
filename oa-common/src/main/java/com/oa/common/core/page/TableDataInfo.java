package com.oa.common.core.page;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 表格分页数据对象
 */
@Data
public class TableDataInfo {

    /**
     * 总记录数
     */
    private long total;

    /**
     * 列表数据
     */
    private List<?> rows;

    /**
     * 消息状态码
     */
    private int code;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 分页
     *
     * @param list  列表数据
     * @param total 总记录数
     */
    public TableDataInfo(List<?> list, long total) {
        this.rows = list;
        this.total = total;
    }

    public TableDataInfo() {
        this.rows = Collections.emptyList();
        this.total = 0L;
    }
}
