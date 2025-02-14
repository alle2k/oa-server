package com.oa.common.error;

/**
 * 系统基础错误码  10000 ~ 19999
 */
public enum BaseCode implements Code {
    REDIRECT(9999, "302请求转发的URL", "302请求转发"),
    SUCCESS(10000, "业务处理成功", "业务处理成功"),
    SYSTEM_FAILED(10001, "网络走神了,请稍后重试", "网络走神了,请稍后重试"),
    TIMED_OUT(10002, "业务处理超时", "系统处理超时，请重试"),
    PARAM_ERROR(10003, "参数错误", "请检查参数是否正确"),

    LOGIN_INVALID(10004, "登录失效", "登录失效,请重新登录"),
    LOGIN_FAILED(10005, "账号或密码错误", "账号或密码错误"),
    PERMISSION_ERROR(10006, "无权限访问", "无权限访问"),
    PASSWORD_ERROR(10007, "旧密码错误", "旧密码错误"),

    DATA_NOT_EXIST(10008, "数据不存在", "数据不存在"),
    BIZ_ERROR(10010, "业务异常", "业务异常"),
    ;

    private final int code;
    private final String info;
    private final String fixTips;

    BaseCode(int code, String info, String fixTips) {
        this.code = code;
        this.info = info;
        this.fixTips = fixTips;
    }

    /**
     * 错误码
     * eg: 200xx gateway
     * 300xx user
     * 400xx order
     * 500xx core
     * 600xx operator
     * 700xx admin
     * <p>
     * universal:
     * 10000 ~ 19999
     * 10000 success
     * 10001 system error
     * 10002 timed out
     * 10003 params error
     * 10004 rpc timeout
     * 10005 rpc invoke error
     */
    @Override
    public int getCode() {
        return this.code;
    }

    /**
     * 错误码说明(内部日志，统计，查看使用)
     */
    @Override
    public String getInfo() {
        return this.info;
    }

    /**
     * 错误码描述，对外输出
     */
    @Override
    public String getFixTips() {
        return this.fixTips;
    }
}
