package org.largeorg.platform.procurement.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.largeorg.platform.audit.annotation.AuditLog;
import org.largeorg.platform.common.Result;
import org.largeorg.platform.procurement.dto.SupplierCreateDTO;
import org.largeorg.platform.procurement.dto.SupplierUpdateDTO;
import org.largeorg.platform.procurement.entity.Supplier;
import org.largeorg.platform.procurement.service.SupplierService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/procurement/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    @SaCheckPermission("procurement:supplier:list")
    public Result<Page<Supplier>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(supplierService.page(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("procurement:supplier:list")
    public Result<Supplier> getById(@PathVariable Long id) {
        return Result.success(supplierService.getById(id));
    }

    @AuditLog(module = "供应商管理", action = "新增供应商")
    @PostMapping
    @SaCheckPermission("procurement:supplier:create")
    public Result<Void> create(@Valid @RequestBody SupplierCreateDTO dto) {
        supplierService.create(dto);
        return Result.success();
    }

    @AuditLog(module = "供应商管理", action = "编辑供应商")
    @PutMapping("/{id}")
    @SaCheckPermission("procurement:supplier:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody SupplierUpdateDTO dto) {
        supplierService.update(id, dto);
        return Result.success();
    }

    @AuditLog(module = "供应商管理", action = "删除供应商")
    @DeleteMapping("/{id}")
    @SaCheckPermission("procurement:supplier:delete")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return Result.success();
    }

    @AuditLog(module = "供应商管理", action = "变更供应商状态")
    @PutMapping("/{id}/status")
    @SaCheckPermission("procurement:supplier:update")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        supplierService.updateStatus(id, status);
        return Result.success();
    }
}
