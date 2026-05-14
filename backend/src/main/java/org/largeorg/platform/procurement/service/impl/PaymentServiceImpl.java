package org.largeorg.platform.procurement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.largeorg.platform.common.BusinessException;
import org.largeorg.platform.common.ErrorCode;
import org.largeorg.platform.procurement.dto.PaymentCreateDTO;
import org.largeorg.platform.procurement.dto.PaymentUpdateDTO;
import org.largeorg.platform.procurement.entity.Contract;
import org.largeorg.platform.procurement.entity.PaymentNode;
import org.largeorg.platform.procurement.mapper.ContractMapper;
import org.largeorg.platform.procurement.mapper.PaymentNodeMapper;
import org.largeorg.platform.procurement.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentNodeMapper paymentMapper;
    private final ContractMapper contractMapper;

    public PaymentServiceImpl(PaymentNodeMapper paymentMapper, ContractMapper contractMapper) {
        this.paymentMapper = paymentMapper;
        this.contractMapper = contractMapper;
    }

    @Override
    public List<PaymentNode> listByContractId(Long contractId) {
        LambdaQueryWrapper<PaymentNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentNode::getContractId, contractId);
        wrapper.orderByAsc(PaymentNode::getCreatedAt);
        return paymentMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void create(PaymentCreateDTO dto) {
        Contract contract = contractMapper.selectById(dto.getContractId());
        if (contract == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "关联的合同不存在");
        }
        PaymentNode node = new PaymentNode();
        node.setContractId(dto.getContractId());
        node.setNodeName(dto.getNodeName());
        node.setAmount(dto.getAmount());
        node.setRatio(dto.getRatio());
        node.setPlannedDate(dto.getPlannedDate());
        node.setStatus("pending");
        paymentMapper.insert(node);
    }

    @Override
    @Transactional
    public void update(Long id, PaymentUpdateDTO dto) {
        PaymentNode node = paymentMapper.selectById(id);
        if (node == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "付款节点不存在");
        }
        if ("paid".equals(node.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "已付款节点不能编辑");
        }
        if (dto.getNodeName() != null) node.setNodeName(dto.getNodeName());
        if (dto.getAmount() != null) node.setAmount(dto.getAmount());
        if (dto.getRatio() != null) node.setRatio(dto.getRatio());
        if (dto.getPlannedDate() != null) node.setPlannedDate(dto.getPlannedDate());
        paymentMapper.updateById(node);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PaymentNode node = paymentMapper.selectById(id);
        if (node == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "付款节点不存在");
        }
        if ("paid".equals(node.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "已付款节点不能删除");
        }
        paymentMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void confirmPay(Long id) {
        PaymentNode node = paymentMapper.selectById(id);
        if (node == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "付款节点不存在");
        }
        if ("paid".equals(node.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该节点已付款");
        }
        node.setStatus("paid");
        node.setActualDate(LocalDate.now());
        paymentMapper.updateById(node);
    }
}
