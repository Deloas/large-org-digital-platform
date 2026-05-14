package org.largeorg.platform.procurement.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.largeorg.platform.audit.annotation.AuditLog;
import org.largeorg.platform.common.Result;
import org.largeorg.platform.procurement.dto.ContractCreateDTO;
import org.largeorg.platform.procurement.dto.ContractUpdateDTO;
import org.largeorg.platform.procurement.entity.Contract;
import org.largeorg.platform.procurement.service.ContractService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/procurement/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    @SaCheckPermission("procurement:contract:list")
    public Result<Page<Contract>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(contractService.page(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("procurement:contract:list")
    public Result<Contract> getById(@PathVariable Long id) {
        return Result.success(contractService.getById(id));
    }

    @AuditLog(module = "合同管理", action = "创建合同")
    @PostMapping
    @SaCheckPermission("procurement:contract:create")
    public Result<Void> create(@Valid @RequestBody ContractCreateDTO dto) {
        contractService.create(dto);
        return Result.success();
    }

    @AuditLog(module = "合同管理", action = "编辑合同")
    @PutMapping("/{id}")
    @SaCheckPermission("procurement:contract:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody ContractUpdateDTO dto) {
        contractService.update(id, dto);
        return Result.success();
    }

    @AuditLog(module = "合同管理", action = "变更合同状态")
    @PutMapping("/{id}/status")
    @SaCheckPermission("procurement:contract:update")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        contractService.updateStatus(id, status);
        return Result.success();
    }
}
