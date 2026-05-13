package org.largeorg.platform.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class AssignPermissionRequest {
    @NotNull(message = "菜单ID列表不能为空")
    private List<Long> menuIds;
}
