package org.largeorg.platform.procurement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.common.BusinessException;
import org.largeorg.platform.common.ErrorCode;
import org.largeorg.platform.procurement.dto.SupplierCreateDTO;
import org.largeorg.platform.procurement.dto.SupplierUpdateDTO;
import org.largeorg.platform.procurement.entity.Supplier;
import org.largeorg.platform.procurement.mapper.SupplierMapper;
import org.largeorg.platform.procurement.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierMapper supplierMapper;

    public SupplierServiceImpl(SupplierMapper supplierMapper) {
        this.supplierMapper = supplierMapper;
    }

    @Override
    public Page<Supplier> page(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Supplier::getName, keyword)
                    .or().like(Supplier::getSupplierNo, keyword)
                    .or().like(Supplier::getContactPerson, keyword));
        }
        wrapper.orderByDesc(Supplier::getCreatedAt);
        Page<Supplier> page = new Page<>(pageNum, pageSize);
        return supplierMapper.selectPage(page, wrapper);
    }

    @Override
    public Supplier getById(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "供应商不存在");
        }
        return supplier;
    }

    @Override
    @Transactional
    public void create(SupplierCreateDTO dto) {
        Supplier supplier = new Supplier();
        supplier.setSupplierNo(generateSupplierNo());
        supplier.setName(dto.getName());
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setContactPhone(dto.getContactPhone());
        supplier.setEmail(dto.getEmail());
        supplier.setAddress(dto.getAddress());
        supplier.setQualification(dto.getQualification());
        supplier.setStatus(1);
        supplierMapper.insert(supplier);
    }

    @Override
    @Transactional
    public void update(Long id, SupplierUpdateDTO dto) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "供应商不存在");
        }
        if (dto.getName() != null) supplier.setName(dto.getName());
        if (dto.getContactPerson() != null) supplier.setContactPerson(dto.getContactPerson());
        if (dto.getContactPhone() != null) supplier.setContactPhone(dto.getContactPhone());
        if (dto.getEmail() != null) supplier.setEmail(dto.getEmail());
        if (dto.getAddress() != null) supplier.setAddress(dto.getAddress());
        if (dto.getQualification() != null) supplier.setQualification(dto.getQualification());
        supplierMapper.updateById(supplier);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "供应商不存在");
        }
        supplierMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "供应商不存在");
        }
        supplier.setStatus(status);
        supplierMapper.updateById(supplier);
    }

    private String generateSupplierNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq = String.format("%04d", System.currentTimeMillis() % 10000);
        return "SUP-" + datePart + "-" + seq;
    }
}
