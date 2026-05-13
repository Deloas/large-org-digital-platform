package org.largeorg.platform.audit.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.audit.dto.LoginLogQueryRequest;
import org.largeorg.platform.audit.dto.OperationLogQueryRequest;
import org.largeorg.platform.audit.service.AuditLogQueryService;
import org.largeorg.platform.audit.vo.LoginLogVo;
import org.largeorg.platform.audit.vo.OperationLogVo;
import org.largeorg.platform.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    public AuditLogController(AuditLogQueryService auditLogQueryService) {
        this.auditLogQueryService = auditLogQueryService;
    }

    @GetMapping("/login-logs")
    @SaCheckPermission("audit:log")
    public Result<Page<LoginLogVo>> pageLoginLogs(LoginLogQueryRequest request) {
        return Result.success(auditLogQueryService.pageLoginLogs(request));
    }

    @GetMapping("/login-logs/{id}")
    @SaCheckPermission("audit:log")
    public Result<LoginLogVo> getLoginLog(@PathVariable Long id) {
        LoginLogVo vo = auditLogQueryService.getLoginLogById(id);
        return vo == null ? Result.error(404, "日志不存在") : Result.success(vo);
    }

    @GetMapping("/operation-logs")
    @SaCheckPermission("audit:log")
    public Result<Page<OperationLogVo>> pageOperationLogs(OperationLogQueryRequest request) {
        return Result.success(auditLogQueryService.pageOperationLogs(request));
    }

    @GetMapping("/operation-logs/{id}")
    @SaCheckPermission("audit:log")
    public Result<OperationLogVo> getOperationLog(@PathVariable Long id) {
        OperationLogVo vo = auditLogQueryService.getOperationLogById(id);
        return vo == null ? Result.error(404, "日志不存在") : Result.success(vo);
    }
}
