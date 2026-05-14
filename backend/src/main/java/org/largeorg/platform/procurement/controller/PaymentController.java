package org.largeorg.platform.procurement.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import org.largeorg.platform.audit.annotation.AuditLog;
import org.largeorg.platform.common.Result;
import org.largeorg.platform.procurement.dto.PaymentCreateDTO;
import org.largeorg.platform.procurement.dto.PaymentUpdateDTO;
import org.largeorg.platform.procurement.entity.PaymentNode;
import org.largeorg.platform.procurement.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procurement/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/contract/{contractId}")
    @SaCheckPermission("procurement:payment:list")
    public Result<List<PaymentNode>> listByContract(@PathVariable Long contractId) {
        return Result.success(paymentService.listByContractId(contractId));
    }

    @AuditLog(module = "付款管理", action = "新增付款节点")
    @PostMapping
    @SaCheckPermission("procurement:payment:create")
    public Result<Void> create(@Valid @RequestBody PaymentCreateDTO dto) {
        paymentService.create(dto);
        return Result.success();
    }

    @AuditLog(module = "付款管理", action = "编辑付款节点")
    @PutMapping("/{id}")
    @SaCheckPermission("procurement:payment:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody PaymentUpdateDTO dto) {
        paymentService.update(id, dto);
        return Result.success();
    }

    @AuditLog(module = "付款管理", action = "删除付款节点")
    @DeleteMapping("/{id}")
    @SaCheckPermission("procurement:payment:update")
    public Result<Void> delete(@PathVariable Long id) {
        paymentService.delete(id);
        return Result.success();
    }

    @AuditLog(module = "付款管理", action = "确认付款")
    @PutMapping("/{id}/pay")
    @SaCheckPermission("procurement:payment:update")
    public Result<Void> confirmPay(@PathVariable Long id) {
        paymentService.confirmPay(id);
        return Result.success();
    }
}
