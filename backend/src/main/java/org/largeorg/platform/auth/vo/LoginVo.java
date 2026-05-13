package org.largeorg.platform.auth.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LoginVo {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private List<String> roles;
    private List<String> permissions;
}
