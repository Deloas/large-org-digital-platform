package org.largeorg.platform.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import org.largeorg.platform.audit.annotation.AuditLog;
import org.largeorg.platform.common.Result;
import org.largeorg.platform.system.dto.AssignPermissionRequest;
import org.largeorg.platform.system.dto.RoleCreateRequest;
import org.largeorg.platform.system.dto.RoleUpdateRequest;
import org.largeorg.platform.system.entity.SysRole;
import org.largeorg.platform.system.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @SaCheckPermission("sys:role:list")
    public Result<List<SysRole>> list() {
        return Result.success(roleService.list());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("sys:role:list")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @AuditLog(module = "角色管理", action = "新增角色")
    @PostMapping
    @SaCheckPermission("sys:role:create")
    public Result<Void> create(@Valid @RequestBody RoleCreateRequest request) {
        roleService.create(request);
        return Result.success();
    }

    @AuditLog(module = "角色管理", action = "修改角色")
    @PutMapping("/{id}")
    @SaCheckPermission("sys:role:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody RoleUpdateRequest request) {
        roleService.update(id, request);
        return Result.success();
    }

    @AuditLog(module = "角色管理", action = "删除角色")
    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:role:delete")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/menus")
    @SaCheckPermission("sys:role:list")
    public Result<List<Long>> getMenuIds(@PathVariable Long id) {
        return Result.success(roleService.getMenuIds(id));
    }

    @AuditLog(module = "角色管理", action = "分配权限")
    @PutMapping("/{id}/menus")
    @SaCheckPermission("sys:role:update")
    public Result<Void> assignMenus(@PathVariable Long id, @Valid @RequestBody AssignPermissionRequest request) {
        roleService.assignMenus(id, request.getMenuIds());
        return Result.success();
    }
}
