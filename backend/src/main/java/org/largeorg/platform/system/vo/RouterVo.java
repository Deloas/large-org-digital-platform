package org.largeorg.platform.system.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouterVo {
    private String name;
    private String path;
    private String component;
    private String redirect;
    private RouterMeta meta;
    private List<RouterVo> children;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouterMeta {
        private String title;
        private String icon;
    }
}
