package org.largeorg.platform.system.service;

import org.largeorg.platform.system.dto.MenuCreateRequest;
import org.largeorg.platform.system.dto.MenuUpdateRequest;
import org.largeorg.platform.system.entity.SysMenu;
import org.largeorg.platform.system.vo.MenuVo;
import org.largeorg.platform.system.vo.RouterVo;

import java.util.List;

public interface MenuService {
    List<MenuVo> tree();
    List<RouterVo> buildRouters();
    SysMenu getById(Long id);
    void create(MenuCreateRequest request);
    void update(Long id, MenuUpdateRequest request);
    void delete(Long id);
}
