package org.largeorg.platform.procurement.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.audit.annotation.AuditLog;
import org.largeorg.platform.common.Result;
import org.largeorg.platform.procurement.dto.ApprovalActionDTO;
import org.largeorg.platform.procurement.entity.ProcurementApproval;
import org.largeorg.platform.procurement.service.ApprovalService;
import org.largeorg.platform.system.entity.SysUser;
import org.largeorg.platform.system.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procurement/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;
    private final UserMapper userMapper;

    public ApprovalController(ApprovalService approvalService, UserMapper userMapper) {
        this.approvalService = approvalService;
        this.userMapper = userMapper;
    }

    @GetMapping("/pending")
    @SaCheckPermission("procurement:approval:pending")
    public Result<Page<ProcurementApproval>> pending(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        String roleCode = getRoleCode();
        Long deptId = getDeptId(userId);
        return Result.success(approvalService.pendingPage(pageNum, pageSize, userId, roleCode, deptId));
    }

    @AuditLog(module = "采购审批", action = "审批通过")
    @PutMapping("/{id}/approve")
    @SaCheckPermission("procurement:approval:approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody ApprovalActionDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        String roleCode = getRoleCode();
        Long deptId = getDeptId(userId);
        approvalService.approve(id, dto, userId, roleCode, deptId);
        return Result.success();
    }

    @AuditLog(module = "采购审批", action = "审批驳回")
    @PutMapping("/{id}/reject")
    @SaCheckPermission("procurement:approval:approve")
    public Result<Void> reject(@PathVariable Long id, @RequestBody ApprovalActionDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        String roleCode = getRoleCode();
        Long deptId = getDeptId(userId);
        approvalService.reject(id, dto, userId, roleCode, deptId);
        return Result.success();
    }

    private String getRoleCode() {
        List<String> roles = StpUtil.getRoleList();
        return roles.isEmpty() ? "" : roles.get(0);
    }

    private Long getDeptId(Long userId) {
        SysUser user = userMapper.selectById(userId);
        return user != null ? user.getDeptId() : null;
    }
}
