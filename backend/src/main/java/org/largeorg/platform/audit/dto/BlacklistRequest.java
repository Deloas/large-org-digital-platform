package org.largeorg.platform.audit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlacklistRequest {
    @NotBlank(message = "IP 地址不能为空")
    private String ipAddress;

    @NotBlank(message = "加黑原因不能为空")
    private String reason;

    private Integer status;
    private LocalDateTime expiresAt;
}
