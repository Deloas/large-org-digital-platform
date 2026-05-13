package org.largeorg.platform.system.dto;

import lombok.Data;

@Data
public class RoleUpdateRequest {
    private String roleName;
    private String description;
    private Integer sortOrder;
}
