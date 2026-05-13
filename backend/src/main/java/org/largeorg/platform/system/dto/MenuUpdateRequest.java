package org.largeorg.platform.system.dto;

import lombok.Data;

@Data
public class MenuUpdateRequest {
    private Long parentId;
    private String name;
    private String type;
    private String path;
    private String component;
    private String icon;
    private String permission;
    private Integer sortOrder;
    private Integer visible;
}
