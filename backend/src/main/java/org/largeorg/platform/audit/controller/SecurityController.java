package org.largeorg.platform.audit.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.largeorg.platform.audit.annotation.AuditLog;
import org.largeorg.platform.audit.dto.AlertQueryRequest;
import org.largeorg.platform.audit.dto.BlacklistQueryRequest;
import org.largeorg.platform.audit.dto.BlacklistRequest;
import org.largeorg.platform.audit.service.IpBlacklistService;
import org.largeorg.platform.audit.service.SecurityAlertService;
import org.largeorg.platform.audit.service.impl.SecurityAlertServiceImpl;
import org.largeorg.platform.audit.vo.IpBlacklistVo;
import org.largeorg.platform.audit.vo.SecurityAlertVo;
import org.largeorg.platform.audit.vo.SecurityDashboardVo;
import org.largeorg.platform.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/audit/security")
public class SecurityController {

    private final SecurityAlertService alertService;
    private final IpBlacklistService blacklistService;

    public SecurityController(SecurityAlertService alertService,
                               IpBlacklistService blacklistService) {
        this.alertService = alertService;
        this.blacklistService = blacklistService;
    }

    // ---- 安全看板 ----

    @GetMapping("/dashboard")
    @SaCheckPermission("audit:dashboard")
    public Result<SecurityDashboardVo> dashboard() {
        SecurityAlertServiceImpl impl = (SecurityAlertServiceImpl) alertService;
        return Result.success(impl.getDashboard());
    }

    // ---- 安全告警 ----

    @GetMapping("/alerts")
    @SaCheckPermission("audit:alert")
    public Result<Page<SecurityAlertVo>> pageAlerts(AlertQueryRequest request) {
        return Result.success(alertService.pageAlerts(request));
    }

    @GetMapping("/alerts/{id}")
    @SaCheckPermission("audit:alert")
    public Result<SecurityAlertVo> getAlert(@PathVariable Long id) {
        SecurityAlertVo vo = alertService.getAlertById(id);
        return vo == null ? Result.error(404, "告警不存在") : Result.success(vo);
    }

    @PutMapping("/alerts/{id}/status")
    @SaCheckPermission("audit:alert")
    @AuditLog(module = "安全审计", action = "处理告警")
    public Result<Void> updateAlertStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        String note = body.get("note");
        String username = StpUtil.getSession().getString("username");
        alertService.updateAlertStatus(id, status, note, username);
        return Result.success();
    }

    // ---- IP 黑名单 ----

    @GetMapping("/blacklist")
    @SaCheckPermission("audit:blacklist")
    public Result<Page<IpBlacklistVo>> pageBlacklist(BlacklistQueryRequest request) {
        return Result.success(blacklistService.pageBlacklist(request));
    }

    @PostMapping("/blacklist")
    @SaCheckPermission("audit:blacklist")
    @AuditLog(module = "安全审计", action = "新增IP黑名单")
    public Result<Void> addBlacklist(@Valid @RequestBody BlacklistRequest request) {
        String username = StpUtil.getSession().getString("username");
        blacklistService.addBlacklist(request, username);
        return Result.success();
    }

    @PutMapping("/blacklist/{id}")
    @SaCheckPermission("audit:blacklist")
    @AuditLog(module = "安全审计", action = "编辑IP黑名单")
    public Result<Void> updateBlacklist(@PathVariable Long id, @Valid @RequestBody BlacklistRequest request) {
        blacklistService.updateBlacklist(id, request);
        return Result.success();
    }

    @DeleteMapping("/blacklist/{id}")
    @SaCheckPermission("audit:blacklist")
    @AuditLog(module = "安全审计", action = "删除IP黑名单")
    public Result<Void> deleteBlacklist(@PathVariable Long id) {
        blacklistService.deleteBlacklist(id);
        return Result.success();
    }
}
