package org.largeorg.platform.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.largeorg.platform.common.Result;
import org.largeorg.platform.system.dto.UserCreateRequest;
import org.largeorg.platform.system.dto.UserUpdateRequest;
import org.largeorg.platform.system.entity.SysUser;
import org.largeorg.platform.system.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @SaCheckPermission("sys:user:list")
    public Result<Page<SysUser>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long deptId) {
        return Result.success(userService.page(pageNum, pageSize, keyword, status, deptId));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("sys:user:list")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("sys:user:create")
    public Result<Void> create(@Valid @RequestBody UserCreateRequest request) {
        userService.create(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @SaCheckPermission("sys:user:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        userService.update(id, request);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @SaCheckPermission("sys:user:update")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    @SaCheckPermission("sys:user:update")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }
}
