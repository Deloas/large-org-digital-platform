package org.largeorg.platform.knowledge.dto;

import lombok.Data;

@Data
public class QaLogQueryRequest {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String username;
    private String status;
    private String startTime;
    private String endTime;
}
