package com.dianping.common.exception;

/**
 * 业务异常基类：可携带业务错误码
 */
public class BaseException extends RuntimeException {

    private final Integer code;

    public BaseException(String msg) {
        super(msg);
        this.code = 1;
    }

    public BaseException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}