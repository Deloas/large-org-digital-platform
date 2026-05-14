package org.largeorg.platform.procurement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.procurement.dto.SupplierCreateDTO;
import org.largeorg.platform.procurement.dto.SupplierUpdateDTO;
import org.largeorg.platform.procurement.entity.Supplier;

public interface SupplierService {
    Page<Supplier> page(int pageNum, int pageSize, String keyword);
    Supplier getById(Long id);
    void create(SupplierCreateDTO dto);
    void update(Long id, SupplierUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
}
