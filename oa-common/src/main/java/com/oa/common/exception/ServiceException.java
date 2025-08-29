package com.oa.common.exception;

import com.oa.common.error.Code;

/**
 * 业务异常
 */
public final class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 错误明细，内部调试错误
     * <p>
     * 和 {@link CommonResult#getDetailMessage()} 一致的设计
     */
    private String detailMessage;

    /**
     * 空构造方法，避免反序列化问题
     */
    public ServiceException() {
    }

    public ServiceException(String message) {
        this.message = message;
    }

    public ServiceException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public ServiceException(Code code) {
        super(code.getFixTips());
        this.code = code.getCode();
        this.message = code.getFixTips();
    }

    public ServiceException(Code code, Throwable e) {
        super(code.getFixTips(), e);
        this.code = code.getCode();
        this.message = code.getFixTips();
    }

    public ServiceException(int code, String msg) {
        super(msg);
        this.code = code;
        this.message = msg;
    }

    public ServiceException(int code, String msg, Throwable e) {
        super(msg, e);
        this.code = code;
        this.message = msg;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

    public ServiceException setMessage(String message) {
        this.message = message;
        return this;
    }

    public ServiceException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }
}