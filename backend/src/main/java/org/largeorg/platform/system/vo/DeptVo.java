package org.largeorg.platform.system.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeptVo {
    private Long id;
    private String deptName;
    private Long parentId;
    private String leaderName;
    private String phone;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private List<DeptVo> children;
}
