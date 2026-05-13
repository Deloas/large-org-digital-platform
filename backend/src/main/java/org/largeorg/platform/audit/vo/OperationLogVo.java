package org.largeorg.platform.audit.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OperationLogVo {
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String action;
    private String requestPath;
    private String requestMethod;
    private String requestParams;
    private String result;
    private String errorMsg;
    private Long costMs;
    private String ip;
    private String userAgent;
    private LocalDateTime createdAt;
}
