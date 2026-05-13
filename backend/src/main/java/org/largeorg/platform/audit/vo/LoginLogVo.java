package org.largeorg.platform.audit.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LoginLogVo {
    private Long id;
    private Long userId;
    private String username;
    private String loginIp;
    private String userAgent;
    private String status;
    private String failReason;
    private LocalDateTime loginTime;
    private LocalDateTime createdAt;
}
