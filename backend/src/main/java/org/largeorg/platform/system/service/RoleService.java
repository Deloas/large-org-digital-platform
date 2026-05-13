package org.largeorg.platform.system.service;

import org.largeorg.platform.system.dto.RoleCreateRequest;
import org.largeorg.platform.system.dto.RoleUpdateRequest;
import org.largeorg.platform.system.entity.SysRole;

import java.util.List;

public interface RoleService {
    List<SysRole> list();
    SysRole getById(Long id);
    void create(RoleCreateRequest request);
    void update(Long id, RoleUpdateRequest request);
    void delete(Long id);
    void assignMenus(Long roleId, List<Long> menuIds);
    List<Long> getMenuIds(Long roleId);
}
