package org.largeorg.platform.procurement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.procurement.dto.ApprovalActionDTO;
import org.largeorg.platform.procurement.entity.ProcurementApproval;

public interface ApprovalService {
    Page<ProcurementApproval> pendingPage(int pageNum, int pageSize, Long userId, String roleCode, Long deptId);
    void approve(Long approvalId, ApprovalActionDTO dto, Long userId, String roleCode, Long deptId);
    void reject(Long approvalId, ApprovalActionDTO dto, Long userId, String roleCode, Long deptId);
}
