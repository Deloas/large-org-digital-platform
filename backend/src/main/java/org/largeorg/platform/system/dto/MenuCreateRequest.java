package org.largeorg.platform.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuCreateRequest {
    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    private String name;

    @NotBlank(message = "菜单类型不能为空")
    private String type;

    private String path;
    private String component;
    private String icon;
    private String permission;
    private Integer sortOrder;
    private Integer visible;
}
