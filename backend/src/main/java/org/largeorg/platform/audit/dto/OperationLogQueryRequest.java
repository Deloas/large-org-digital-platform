package org.largeorg.platform.audit.dto;

import lombok.Data;

@Data
public class OperationLogQueryRequest {
    private Integer page = 1;
    private Integer pageSize = 15;
    private String username;
    private String module;
    private String action;
    private String requestPath;
    private String result;
    private String startTime;
    private String endTime;
}
