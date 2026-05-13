package org.largeorg.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.largeorg.platform.common.BusinessException;
import org.largeorg.platform.common.ErrorCode;
import org.largeorg.platform.system.dto.MenuCreateRequest;
import org.largeorg.platform.system.dto.MenuUpdateRequest;
import org.largeorg.platform.system.entity.SysMenu;
import org.largeorg.platform.system.mapper.MenuMapper;
import org.largeorg.platform.system.service.MenuService;
import cn.dev33.satoken.stp.StpUtil;
import org.largeorg.platform.system.vo.MenuVo;
import org.largeorg.platform.system.vo.RouterVo;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    public MenuServiceImpl(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @Override
    public List<MenuVo> tree() {
        List<SysMenu> all = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSortOrder));
        return buildMenuTree(all);
    }

    @Override
    public List<RouterVo> buildRouters() {
        long userId = StpUtil.getLoginIdAsLong();
        List<SysMenu> all = menuMapper.selectMenusByUserId(userId);
        return all.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .map(m -> convertToRouter(m, all))
                .collect(Collectors.toList());
    }

    private RouterVo convertToRouter(SysMenu menu, List<SysMenu> all) {
        List<RouterVo> children = all.stream()
                .filter(m -> Objects.equals(m.getParentId(), menu.getId()))
                .map(m -> convertToRouter(m, all))
                .collect(Collectors.toList());

        return RouterVo.builder()
                .name(menu.getPath() != null ? menu.getPath().replace("/", "") : menu.getName())
                .path(menu.getPath())
                .component(menu.getComponent())
                .redirect(children.isEmpty() ? null : children.get(0).getPath())
                .meta(RouterVo.RouterMeta.builder()
                        .title(menu.getName())
                        .icon(menu.getIcon())
                        .build())
                .children(children.isEmpty() ? null : children)
                .build();
    }

    private List<MenuVo> buildMenuTree(List<SysMenu> all) {
        Map<Long, MenuVo> map = all.stream().map(m -> {
            MenuVo vo = new MenuVo();
            vo.setId(m.getId());
            vo.setParentId(m.getParentId());
            vo.setName(m.getName());
            vo.setType(m.getType());
            vo.setPath(m.getPath());
            vo.setComponent(m.getComponent());
            vo.setIcon(m.getIcon());
            vo.setPermission(m.getPermission());
            vo.setSortOrder(m.getSortOrder());
            vo.setVisible(m.getVisible());
            vo.setStatus(m.getStatus());
            vo.setCreatedAt(m.getCreatedAt());
            vo.setChildren(new ArrayList<>());
            return vo;
        }).collect(Collectors.toMap(MenuVo::getId, v -> v));

        List<MenuVo> roots = new ArrayList<>();
        for (MenuVo vo : map.values()) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                roots.add(vo);
            } else {
                MenuVo parent = map.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }
        return roots;
    }

    @Override
    public SysMenu getById(Long id) {
        return menuMapper.selectById(id);
    }

    @Override
    public void create(MenuCreateRequest request) {
        SysMenu menu = new SysMenu();
        menu.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        menu.setName(request.getName());
        menu.setType(request.getType());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setIcon(request.getIcon());
        menu.setPermission(request.getPermission());
        menu.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        menu.setVisible(request.getVisible() != null ? request.getVisible() : 1);
        menu.setStatus(1);
        menuMapper.insert(menu);
    }

    @Override
    public void update(Long id, MenuUpdateRequest request) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "菜单不存在");
        }
        if (request.getParentId() != null) menu.setParentId(request.getParentId());
        if (request.getName() != null) menu.setName(request.getName());
        if (request.getType() != null) menu.setType(request.getType());
        if (request.getPath() != null) menu.setPath(request.getPath());
        if (request.getComponent() != null) menu.setComponent(request.getComponent());
        if (request.getIcon() != null) menu.setIcon(request.getIcon());
        if (request.getPermission() != null) menu.setPermission(request.getPermission());
        if (request.getSortOrder() != null) menu.setSortOrder(request.getSortOrder());
        if (request.getVisible() != null) menu.setVisible(request.getVisible());
        menuMapper.updateById(menu);
    }

    @Override
    public void delete(Long id) {
        Long childCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "存在子菜单，无法删除");
        }
        menuMapper.deleteById(id);
    }
}
