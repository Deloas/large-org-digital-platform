package org.largeorg.platform.audit.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SecurityAlertVo {
    private Long id;
    private String alertType;
    private String severity;
    private String title;
    private String detail;
    private String relatedUser;
    private String relatedIp;
    private String status;
    private String handler;
    private String handleNote;
    private Integer duplicateCount;
    private LocalDateTime firstTime;
    private LocalDateTime lastTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
