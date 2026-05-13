package org.largeorg.platform.system.dto;

import lombok.Data;

@Data
public class DeptUpdateRequest {
    private String deptName;
    private Long parentId;
    private String leaderName;
    private String phone;
    private Integer sortOrder;
}
