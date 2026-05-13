package org.largeorg.platform.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import org.largeorg.platform.audit.annotation.AuditLog;
import org.largeorg.platform.common.Result;
import org.largeorg.platform.system.dto.DeptCreateRequest;
import org.largeorg.platform.system.dto.DeptUpdateRequest;
import org.largeorg.platform.system.entity.SysDept;
import org.largeorg.platform.system.service.DeptService;
import org.largeorg.platform.system.vo.DeptVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/depts")
public class DeptController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    @GetMapping
    @SaCheckPermission("sys:dept:list")
    public Result<List<DeptVo>> tree() {
        return Result.success(deptService.tree());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("sys:dept:list")
    public Result<SysDept> getById(@PathVariable Long id) {
        return Result.success(deptService.getById(id));
    }

    @AuditLog(module = "部门管理", action = "新增部门")
    @PostMapping
    @SaCheckPermission("sys:dept:create")
    public Result<Void> create(@Valid @RequestBody DeptCreateRequest request) {
        deptService.create(request);
        return Result.success();
    }

    @AuditLog(module = "部门管理", action = "修改部门")
    @PutMapping("/{id}")
    @SaCheckPermission("sys:dept:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody DeptUpdateRequest request) {
        deptService.update(id, request);
        return Result.success();
    }

    @AuditLog(module = "部门管理", action = "删除部门")
    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:dept:delete")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return Result.success();
    }
}
