package org.largeorg.platform.config;

import cn.dev33.satoken.stp.StpInterface;
import org.largeorg.platform.system.mapper.RoleMapper;
import org.largeorg.platform.system.mapper.MenuMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;

    public StpInterfaceImpl(RoleMapper roleMapper, MenuMapper menuMapper) {
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        List<String> permissions = menuMapper.selectPermissionsByUserId(userId);
        return permissions != null ? permissions : new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        List<String> roles = roleMapper.selectRoleCodesByUserId(userId);
        return roles != null ? roles : new ArrayList<>();
    }
}
