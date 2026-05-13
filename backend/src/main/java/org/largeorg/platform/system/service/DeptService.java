package org.largeorg.platform.system.service;

import org.largeorg.platform.system.dto.DeptCreateRequest;
import org.largeorg.platform.system.dto.DeptUpdateRequest;
import org.largeorg.platform.system.entity.SysDept;
import org.largeorg.platform.system.vo.DeptVo;

import java.util.List;

public interface DeptService {
    List<DeptVo> tree();
    SysDept getById(Long id);
    void create(DeptCreateRequest request);
    void update(Long id, DeptUpdateRequest request);
    void delete(Long id);
}
