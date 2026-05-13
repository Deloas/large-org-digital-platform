package org.largeorg.platform.common;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "success"),

    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(4001, "缺少必要参数"),
    PARAM_INVALID(4002, "参数格式不正确"),

    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),

    NOT_FOUND(404, "资源不存在"),

    METHOD_NOT_ALLOWED(405, "不支持的请求方法"),

    LOGIN_FAILED(4011, "用户名或密码错误"),
    ACCOUNT_DISABLED(4012, "账号已被禁用"),
    TOKEN_INVALID(4013, "token无效或已过期"),
    PASSWORD_SAME(4003, "新密码不能与旧密码相同"),

    SYSTEM_ERROR(500, "系统内部错误"),
    DB_ERROR(5001, "数据库操作失败"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
