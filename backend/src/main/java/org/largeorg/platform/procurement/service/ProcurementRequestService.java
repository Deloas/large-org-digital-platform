package org.largeorg.platform.procurement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.procurement.dto.RequestCreateDTO;
import org.largeorg.platform.procurement.dto.RequestQueryDTO;
import org.largeorg.platform.procurement.dto.RequestUpdateDTO;
import org.largeorg.platform.procurement.entity.ProcurementRequest;

public interface ProcurementRequestService {
    Page<ProcurementRequest> page(int pageNum, int pageSize, RequestQueryDTO query, Long userId, String roleCode);
    ProcurementRequest getById(Long id);
    void create(RequestCreateDTO dto, Long userId, Long deptId);
    void update(Long id, RequestUpdateDTO dto, Long userId);
    void delete(Long id, Long userId);
    void submit(Long id, Long userId);
    void withdraw(Long id, Long userId);
}
