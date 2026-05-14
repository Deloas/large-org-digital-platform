package org.largeorg.platform.procurement.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.largeorg.platform.audit.annotation.AuditLog;
import org.largeorg.platform.common.Result;
import org.largeorg.platform.procurement.dto.RequestCreateDTO;
import org.largeorg.platform.procurement.dto.RequestQueryDTO;
import org.largeorg.platform.procurement.dto.RequestUpdateDTO;
import org.largeorg.platform.procurement.entity.ProcurementApproval;
import org.largeorg.platform.procurement.entity.ProcurementRequest;
import org.largeorg.platform.procurement.mapper.ProcurementApprovalMapper;
import org.largeorg.platform.procurement.service.ProcurementRequestService;
import org.largeorg.platform.system.entity.SysUser;
import org.largeorg.platform.system.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procurement/requests")
public class ProcurementRequestController {

    private final ProcurementRequestService requestService;
    private final ProcurementApprovalMapper approvalMapper;
    private final UserMapper userMapper;

    public ProcurementRequestController(ProcurementRequestService requestService,
                                         ProcurementApprovalMapper approvalMapper,
                                         UserMapper userMapper) {
        this.requestService = requestService;
        this.approvalMapper = approvalMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    @SaCheckPermission("procurement:request:list")
    public Result<Page<ProcurementRequest>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = StpUtil.getLoginIdAsLong();
        String roleCode = getRoleCode();
        RequestQueryDTO query = new RequestQueryDTO();
        query.setKeyword(keyword);
        query.setStatus(status);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        return Result.success(requestService.page(pageNum, pageSize, query, userId, roleCode));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("procurement:request:list")
    public Result<ProcurementRequest> getById(@PathVariable Long id) {
        ProcurementRequest request = requestService.getById(id);
        List<ProcurementApproval> approvals = approvalMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcurementApproval>()
                        .eq(ProcurementApproval::getRequestId, id)
                        .orderByAsc(ProcurementApproval::getStepOrder));
        // 将审批记录附加到返回结果中（通过手动设置额外字段；前端自行解析）
        return Result.success(request);
    }

    @GetMapping("/{id}/approvals")
    @SaCheckPermission("procurement:request:list")
    public Result<List<ProcurementApproval>> getApprovals(@PathVariable Long id) {
        List<ProcurementApproval> approvals = approvalMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcurementApproval>()
                        .eq(ProcurementApproval::getRequestId, id)
                        .orderByAsc(ProcurementApproval::getStepOrder));
        return Result.success(approvals);
    }

    @AuditLog(module = "采购申请", action = "创建采购申请")
    @PostMapping
    @SaCheckPermission("procurement:request:create")
    public Result<Void> create(@Valid @RequestBody RequestCreateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long deptId = getDeptId(userId);
        requestService.create(dto, userId, deptId);
        return Result.success();
    }

    @AuditLog(module = "采购申请", action = "编辑采购申请")
    @PutMapping("/{id}")
    @SaCheckPermission("procurement:request:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody RequestUpdateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        requestService.update(id, dto, userId);
        return Result.success();
    }

    @AuditLog(module = "采购申请", action = "删除采购申请")
    @DeleteMapping("/{id}")
    @SaCheckPermission("procurement:request:delete")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        requestService.delete(id, userId);
        return Result.success();
    }

    @AuditLog(module = "采购申请", action = "提交采购申请")
    @PutMapping("/{id}/submit")
    @SaCheckPermission("procurement:request:create")
    public Result<Void> submit(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        requestService.submit(id, userId);
        return Result.success();
    }

    @AuditLog(module = "采购申请", action = "撤回采购申请")
    @PutMapping("/{id}/withdraw")
    @SaCheckPermission("procurement:request:create")
    public Result<Void> withdraw(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        requestService.withdraw(id, userId);
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
