package org.largeorg.platform.procurement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.procurement.dto.ContractCreateDTO;
import org.largeorg.platform.procurement.dto.ContractUpdateDTO;
import org.largeorg.platform.procurement.entity.Contract;

public interface ContractService {
    Page<Contract> page(int pageNum, int pageSize, String keyword, String status);
    Contract getById(Long id);
    void create(ContractCreateDTO dto);
    void update(Long id, ContractUpdateDTO dto);
    void updateStatus(Long id, String status);
}
