package org.largeorg.platform.system.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuVo {
    private Long id;
    private Long parentId;
    private String name;
    private String type;
    private String path;
    private String component;
    private String icon;
    private String permission;
    private Integer sortOrder;
    private Integer visible;
    private Integer status;
    private LocalDateTime createdAt;
    private List<MenuVo> children;
}
