package org.largeorg.platform.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeptCreateRequest {
    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    private Long parentId;
    private String leaderName;
    private String phone;
    private Integer sortOrder;
}
