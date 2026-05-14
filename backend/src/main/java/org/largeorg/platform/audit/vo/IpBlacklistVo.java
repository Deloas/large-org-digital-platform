package org.largeorg.platform.audit.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IpBlacklistVo {
    private Long id;
    private String ipAddress;
    private String reason;
    private Integer status;
    private LocalDateTime expiresAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
