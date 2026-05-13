package org.largeorg.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.system.dto.UserCreateRequest;
import org.largeorg.platform.system.dto.UserUpdateRequest;
import org.largeorg.platform.system.entity.SysUser;

public interface UserService {
    Page<SysUser> page(int pageNum, int pageSize, String keyword, Integer status, Long deptId);
    SysUser getById(Long id);
    void create(UserCreateRequest request);
    void update(Long id, UserUpdateRequest request);
    void updateStatus(Long id, Integer status);
    void resetPassword(Long id);
}
