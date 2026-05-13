package org.largeorg.platform.audit.dto;

import lombok.Data;

@Data
public class LoginLogQueryRequest {
    private Integer page = 1;
    private Integer pageSize = 15;
    private String username;
    private String ip;
    private String status;
    private String startTime;
    private String endTime;
}
