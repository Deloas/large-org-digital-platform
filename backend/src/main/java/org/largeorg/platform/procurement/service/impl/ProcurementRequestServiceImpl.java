package org.largeorg.platform.procurement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.common.BusinessException;
import org.largeorg.platform.common.ErrorCode;
import org.largeorg.platform.procurement.dto.RequestCreateDTO;
import org.largeorg.platform.procurement.dto.RequestQueryDTO;
import org.largeorg.platform.procurement.dto.RequestUpdateDTO;
import org.largeorg.platform.procurement.entity.ProcurementApproval;
import org.largeorg.platform.procurement.entity.ProcurementRequest;
import org.largeorg.platform.procurement.mapper.ProcurementApprovalMapper;
import org.largeorg.platform.procurement.mapper.ProcurementRequestMapper;
import org.largeorg.platform.procurement.service.ProcurementRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ProcurementRequestServiceImpl implements ProcurementRequestService {

    private static final BigDecimal LEVEL1 = new BigDecimal("5000");
    private static final BigDecimal LEVEL2 = new BigDecimal("50000");

    private final ProcurementRequestMapper requestMapper;
    private final ProcurementApprovalMapper approvalMapper;

    public ProcurementRequestServiceImpl(ProcurementRequestMapper requestMapper,
                                          ProcurementApprovalMapper approvalMapper) {
        this.requestMapper = requestMapper;
        this.approvalMapper = approvalMapper;
    }

    @Override
    public Page<ProcurementRequest> page(int pageNum, int pageSize, RequestQueryDTO query,
                                          Long userId, String roleCode) {
        LambdaQueryWrapper<ProcurementRequest> wrapper = new LambdaQueryWrapper<>();
        applyRoleFilter(wrapper, userId, roleCode);
        if (query != null) {
            if (StringUtils.hasText(query.getKeyword())) {
                wrapper.and(w -> w.like(ProcurementRequest::getTitle, query.getKeyword())
                        .or().like(ProcurementRequest::getRequestNo, query.getKeyword()));
            }
            if (StringUtils.hasText(query.getStatus())) {
                wrapper.eq(ProcurementRequest::getStatus, query.getStatus());
            }
            if (query.getAmountMin() != null) {
                wrapper.ge(ProcurementRequest::getAmount, query.getAmountMin());
            }
            if (query.getAmountMax() != null) {
                wrapper.le(ProcurementRequest::getAmount, query.getAmountMax());
            }
            if (StringUtils.hasText(query.getStartDate())) {
                wrapper.ge(ProcurementRequest::getCreatedAt, query.getStartDate());
            }
            if (StringUtils.hasText(query.getEndDate())) {
                wrapper.le(ProcurementRequest::getCreatedAt, query.getEndDate() + " 23:59:59");
            }
        }
        wrapper.orderByDesc(ProcurementRequest::getCreatedAt);
        Page<ProcurementRequest> page = new Page<>(pageNum, pageSize);
        return requestMapper.selectPage(page, wrapper);
    }

    private void applyRoleFilter(LambdaQueryWrapper<ProcurementRequest> wrapper, Long userId, String roleCode) {
        if ("admin".equals(roleCode) || "procurement".equals(roleCode)) {
            return;
        }
        if ("employee".equals(roleCode)) {
            wrapper.eq(ProcurementRequest::getApplicantId, userId);
        }
    }

    @Override
    public ProcurementRequest getById(Long id) {
        ProcurementRequest request = requestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "采购申请不存在");
        }
        return request;
    }

    @Override
    @Transactional
    public void create(RequestCreateDTO dto, Long userId, Long deptId) {
        ProcurementRequest request = new ProcurementRequest();
        request.setRequestNo(generateRequestNo());
        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setAmount(dto.getAmount());
        request.setCategory(dto.getCategory());
        request.setStatus("draft");
        request.setApplicantId(userId);
        request.setDeptId(deptId);
        request.setCurrentStep(0);
        request.setTotalSteps(0);
        requestMapper.insert(request);
    }

    @Override
    @Transactional
    public void update(Long id, RequestUpdateDTO dto, Long userId) {
        ProcurementRequest request = requestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "采购申请不存在");
        }
        if (!"draft".equals(request.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "仅草稿状态的申请可编辑");
        }
        if (!request.getApplicantId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅申请人可编辑自己的申请");
        }
        if (dto.getTitle() != null) request.setTitle(dto.getTitle());
        if (dto.getDescription() != null) request.setDescription(dto.getDescription());
        if (dto.getAmount() != null) request.setAmount(dto.getAmount());
        if (dto.getCategory() != null) request.setCategory(dto.getCategory());
        requestMapper.updateById(request);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        ProcurementRequest request = requestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "采购申请不存在");
        }
        if (!"draft".equals(request.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "仅草稿状态的申请可删除");
        }
        if (!request.getApplicantId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅申请人可删除自己的申请");
        }
        requestMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void submit(Long id, Long userId) {
        ProcurementRequest request = requestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "采购申请不存在");
        }
        if (!"draft".equals(request.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "仅草稿状态的申请可提交");
        }
        if (!request.getApplicantId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅申请人可提交自己的申请");
        }
        int totalSteps = calculateSteps(request.getAmount());
        request.setStatus("pending");
        request.setCurrentStep(1);
        request.setTotalSteps(totalSteps);
        requestMapper.updateById(request);

        // 生成审批记录
        createApprovalRecord(request.getId(), 1, "dept_manager");
        if (totalSteps >= 2) {
            createApprovalRecord(request.getId(), 2, "finance");
        }
        if (totalSteps >= 3) {
            createApprovalRecord(request.getId(), 3, "procurement");
        }
    }

    @Override
    @Transactional
    public void withdraw(Long id, Long userId) {
        ProcurementRequest request = requestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "采购申请不存在");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "仅审批中的申请可撤回");
        }
        if (!request.getApplicantId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅申请人可撤回自己的申请");
        }
        request.setStatus("withdrawn");
        request.setCurrentStep(0);
        requestMapper.updateById(request);

        // 删除未完成的审批记录
        approvalMapper.delete(new LambdaQueryWrapper<ProcurementApproval>()
                .eq(ProcurementApproval::getRequestId, id)
                .eq(ProcurementApproval::getStatus, "pending"));
    }

    private int calculateSteps(BigDecimal amount) {
        if (amount.compareTo(LEVEL1) < 0) return 1;
        if (amount.compareTo(LEVEL2) <= 0) return 2;
        return 3;
    }

    private void createApprovalRecord(Long requestId, int stepOrder, String expectedRole) {
        ProcurementApproval approval = new ProcurementApproval();
        approval.setRequestId(requestId);
        approval.setStepOrder(stepOrder);
        approval.setExpectedRole(expectedRole);
        approval.setStatus("pending");
        approvalMapper.insert(approval);
    }

    private String generateRequestNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq = String.format("%04d", System.currentTimeMillis() % 10000);
        return "PR-" + datePart + "-" + seq;
    }
}
