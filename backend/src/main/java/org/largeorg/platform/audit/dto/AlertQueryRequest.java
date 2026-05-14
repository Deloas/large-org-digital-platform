package org.largeorg.platform.audit.dto;

import lombok.Data;

@Data
public class AlertQueryRequest {
    private Integer page = 1;
    private Integer pageSize = 15;
    private String alertType;
    private String severity;
    private String status;
    private String relatedUser;
    private String relatedIp;
    private String startTime;
    private String endTime;
}
