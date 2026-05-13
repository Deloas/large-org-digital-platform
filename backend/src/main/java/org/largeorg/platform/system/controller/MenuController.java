package org.largeorg.platform.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import org.largeorg.platform.audit.annotation.AuditLog;
import org.largeorg.platform.common.Result;
import org.largeorg.platform.system.dto.MenuCreateRequest;
import org.largeorg.platform.system.dto.MenuUpdateRequest;
import org.largeorg.platform.system.entity.SysMenu;
import org.largeorg.platform.system.service.MenuService;
import org.largeorg.platform.system.vo.MenuVo;
import org.largeorg.platform.system.vo.RouterVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    @SaCheckPermission("sys:menu:list")
    public Result<List<MenuVo>> tree() {
        return Result.success(menuService.tree());
    }

    @GetMapping("/routers")
    public Result<List<RouterVo>> routers() {
        return Result.success(menuService.buildRouters());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("sys:menu:list")
    public Result<SysMenu> getById(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @AuditLog(module = "菜单管理", action = "新增菜单")
    @PostMapping
    @SaCheckPermission("sys:menu:create")
    public Result<Void> create(@Valid @RequestBody MenuCreateRequest request) {
        menuService.create(request);
        return Result.success();
    }

    @AuditLog(module = "菜单管理", action = "修改菜单")
    @PutMapping("/{id}")
    @SaCheckPermission("sys:menu:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody MenuUpdateRequest request) {
        menuService.update(id, request);
        return Result.success();
    }

    @AuditLog(module = "菜单管理", action = "删除菜单")
    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:menu:delete")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}
