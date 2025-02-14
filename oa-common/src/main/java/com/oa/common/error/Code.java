package com.oa.common.error;

/**
 * 统一错误码接口，所有业务错误码均实现该接口
 */
public interface Code {
    /**
     * 错误码
     */
    int getCode();

    /**
     * 错误码说明(内部日志，统计，查看使用)
     */
    String getInfo();

    /**
     * 错误码描述，对外输出
     */
    String getFixTips();
}
