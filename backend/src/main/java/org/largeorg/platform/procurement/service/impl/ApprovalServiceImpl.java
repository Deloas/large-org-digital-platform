package org.largeorg.platform.procurement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.common.BusinessException;
import org.largeorg.platform.common.ErrorCode;
import org.largeorg.platform.procurement.dto.ApprovalActionDTO;
import org.largeorg.platform.procurement.entity.ProcurementApproval;
import org.largeorg.platform.procurement.entity.ProcurementRequest;
import org.largeorg.platform.procurement.mapper.ProcurementApprovalMapper;
import org.largeorg.platform.procurement.mapper.ProcurementRequestMapper;
import org.largeorg.platform.procurement.service.ApprovalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApprovalServiceImpl implements ApprovalService {

    private final ProcurementApprovalMapper approvalMapper;
    private final ProcurementRequestMapper requestMapper;

    public ApprovalServiceImpl(ProcurementApprovalMapper approvalMapper,
                                ProcurementRequestMapper requestMapper) {
        this.approvalMapper = approvalMapper;
        this.requestMapper = requestMapper;
    }

    @Override
    public Page<ProcurementApproval> pendingPage(int pageNum, int pageSize,
                                                   Long userId, String roleCode, Long deptId) {
        LambdaQueryWrapper<ProcurementApproval> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcurementApproval::getStatus, "pending");

        if ("admin".equals(roleCode)) {
            // admin sees all pending
        } else if ("dept_manager".equals(roleCode)) {
            wrapper.eq(ProcurementApproval::getExpectedRole, "dept_manager");
            // Filter by dept: only show approvals for requests in the manager's dept
            List<Long> requestIds = getRequestIdsByDept(deptId);
            if (requestIds.isEmpty()) {
                requestIds.add(-1L);
            }
            wrapper.in(ProcurementApproval::getRequestId, requestIds);
        } else if ("finance".equals(roleCode)) {
            wrapper.eq(ProcurementApproval::getExpectedRole, "finance");
        } else if ("procurement".equals(roleCode)) {
            wrapper.eq(ProcurementApproval::getExpectedRole, "procurement");
        } else {
            // employee and others should not access
            Page<ProcurementApproval> empty = new Page<>(pageNum, pageSize);
            empty.setTotal(0);
            return empty;
        }

        wrapper.orderByAsc(ProcurementApproval::getCreatedAt);
        Page<ProcurementApproval> page = new Page<>(pageNum, pageSize);
        return approvalMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void approve(Long approvalId, ApprovalActionDTO dto, Long userId,
                         String roleCode, Long deptId) {
        ProcurementApproval approval = approvalMapper.selectById(approvalId);
        if (approval == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "审批记录不存在");
        }
        if (!"pending".equals(approval.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该审批已处理");
        }

        ProcurementRequest request = requestMapper.selectById(approval.getRequestId());
        if (request == null || !"pending".equals(request.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "采购申请状态异常");
        }

        // 验证审批人角色是否匹配
        if (!roleMatches(roleCode, approval.getExpectedRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "您没有权限审批此步骤");
        }

        // 部门负责人只能审批本部门申请
        if ("dept_manager".equals(approval.getExpectedRole()) && !deptId.equals(request.getDeptId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能审批本部门的采购申请");
        }

        // 标记当前审批通过
        approval.setStatus("approved");
        approval.setApproverId(userId);
        approval.setComment(dto.getComment());
        approval.setApprovedAt(LocalDateTime.now());
        approvalMapper.updateById(approval);

        // 判断是否还有下一步
        if (request.getCurrentStep() < request.getTotalSteps()) {
            request.setCurrentStep(request.getCurrentStep() + 1);
            requestMapper.updateById(request);
        } else {
            // 全部审批通过
            request.setStatus("approved");
            requestMapper.updateById(request);
        }
    }

    @Override
    @Transactional
    public void reject(Long approvalId, ApprovalActionDTO dto, Long userId,
                        String roleCode, Long deptId) {
        ProcurementApproval approval = approvalMapper.selectById(approvalId);
        if (approval == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "审批记录不存在");
        }
        if (!"pending".equals(approval.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该审批已处理");
        }

        ProcurementRequest request = requestMapper.selectById(approval.getRequestId());
        if (request == null || !"pending".equals(request.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "采购申请状态异常");
        }

        if (!roleMatches(roleCode, approval.getExpectedRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "您没有权限审批此步骤");
        }

        if ("dept_manager".equals(approval.getExpectedRole()) && !deptId.equals(request.getDeptId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能审批本部门的采购申请");
        }

        // 驳回
        approval.setStatus("rejected");
        approval.setApproverId(userId);
        approval.setComment(dto.getComment());
        approval.setApprovedAt(LocalDateTime.now());
        approvalMapper.updateById(approval);

        request.setStatus("rejected");
        requestMapper.updateById(request);
    }

    private boolean roleMatches(String userRole, String expectedRole) {
        if ("admin".equals(userRole)) return true;
        return userRole.equals(expectedRole);
    }

    private List<Long> getRequestIdsByDept(Long deptId) {
        LambdaQueryWrapper<ProcurementRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcurementRequest::getDeptId, deptId);
        wrapper.eq(ProcurementRequest::getStatus, "pending");
        return requestMapper.selectList(wrapper).stream()
                .map(ProcurementRequest::getId)
                .toList();
    }
}
