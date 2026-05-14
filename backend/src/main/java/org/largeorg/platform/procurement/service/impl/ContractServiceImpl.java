package org.largeorg.platform.procurement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.common.BusinessException;
import org.largeorg.platform.common.ErrorCode;
import org.largeorg.platform.procurement.dto.ContractCreateDTO;
import org.largeorg.platform.procurement.dto.ContractUpdateDTO;
import org.largeorg.platform.procurement.entity.Contract;
import org.largeorg.platform.procurement.entity.ProcurementRequest;
import org.largeorg.platform.procurement.entity.Supplier;
import org.largeorg.platform.procurement.mapper.ContractMapper;
import org.largeorg.platform.procurement.mapper.ProcurementRequestMapper;
import org.largeorg.platform.procurement.mapper.SupplierMapper;
import org.largeorg.platform.procurement.service.ContractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ContractServiceImpl implements ContractService {

    private final ContractMapper contractMapper;
    private final ProcurementRequestMapper requestMapper;
    private final SupplierMapper supplierMapper;

    public ContractServiceImpl(ContractMapper contractMapper,
                                ProcurementRequestMapper requestMapper,
                                SupplierMapper supplierMapper) {
        this.contractMapper = contractMapper;
        this.requestMapper = requestMapper;
        this.supplierMapper = supplierMapper;
    }

    @Override
    public Page<Contract> page(int pageNum, int pageSize, String keyword, String status) {
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Contract::getTitle, keyword)
                    .or().like(Contract::getContractNo, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Contract::getStatus, status);
        }
        wrapper.orderByDesc(Contract::getCreatedAt);
        Page<Contract> page = new Page<>(pageNum, pageSize);
        return contractMapper.selectPage(page, wrapper);
    }

    @Override
    public Contract getById(Long id) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "合同不存在");
        }
        return contract;
    }

    @Override
    @Transactional
    public void create(ContractCreateDTO dto) {
        // 校验关联采购申请
        ProcurementRequest request = requestMapper.selectById(dto.getRequestId());
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "关联的采购申请不存在");
        }
        if (!"approved".equals(request.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "仅审批通过的采购申请可创建合同");
        }
        // 校验关联供应商
        Supplier supplier = supplierMapper.selectById(dto.getSupplierId());
        if (supplier == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "关联的供应商不存在");
        }
        Contract contract = new Contract();
        contract.setContractNo(generateContractNo());
        contract.setRequestId(dto.getRequestId());
        contract.setSupplierId(dto.getSupplierId());
        contract.setTitle(dto.getTitle());
        contract.setAmount(dto.getAmount());
        contract.setSignedDate(dto.getSignedDate());
        contract.setExpiryDate(dto.getExpiryDate());
        contract.setStatus("active");
        contractMapper.insert(contract);
    }

    @Override
    @Transactional
    public void update(Long id, ContractUpdateDTO dto) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "合同不存在");
        }
        if (dto.getTitle() != null) contract.setTitle(dto.getTitle());
        if (dto.getAmount() != null) contract.setAmount(dto.getAmount());
        if (dto.getSignedDate() != null) contract.setSignedDate(dto.getSignedDate());
        if (dto.getExpiryDate() != null) contract.setExpiryDate(dto.getExpiryDate());
        contractMapper.updateById(contract);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "合同不存在");
        }
        contract.setStatus(status);
        contractMapper.updateById(contract);
    }

    private String generateContractNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq = String.format("%04d", System.currentTimeMillis() % 10000);
        return "CON-" + datePart + "-" + seq;
    }
}
