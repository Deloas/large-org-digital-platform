package org.largeorg.platform.system.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String realName;
    private String email;
    private String phone;
    private Long deptId;
    private Long roleId;
}
