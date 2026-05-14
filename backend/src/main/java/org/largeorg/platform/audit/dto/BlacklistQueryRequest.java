package org.largeorg.platform.audit.dto;

import lombok.Data;

@Data
public class BlacklistQueryRequest {
    private Integer page = 1;
    private Integer pageSize = 15;
    private String ipAddress;
    private Integer status;
}
