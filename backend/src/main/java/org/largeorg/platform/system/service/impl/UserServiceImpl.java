package org.largeorg.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.common.BusinessException;
import org.largeorg.platform.common.ErrorCode;
import org.largeorg.platform.system.dto.UserCreateRequest;
import org.largeorg.platform.system.dto.UserUpdateRequest;
import org.largeorg.platform.system.entity.SysUser;
import org.largeorg.platform.system.entity.SysUserRole;
import org.largeorg.platform.system.mapper.UserMapper;
import org.largeorg.platform.system.mapper.UserRoleMapper;
import org.largeorg.platform.system.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    public UserServiceImpl(UserMapper userMapper, UserRoleMapper userRoleMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public Page<SysUser> page(int pageNum, int pageSize, String keyword, Integer status, Long deptId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword));
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        if (deptId != null) {
            wrapper.eq(SysUser::getDeptId, deptId);
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        Page<SysUser> result = userMapper.selectPage(page, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    @Override
    public SysUser getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    @Transactional
    public void create(UserCreateRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setDeptId(request.getDeptId());
        user.setStatus(1);
        userMapper.insert(user);

        if (request.getRoleId() != null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(request.getRoleId());
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    @Transactional
    public void update(Long id, UserUpdateRequest request) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getDeptId() != null) user.setDeptId(request.getDeptId());
        userMapper.updateById(user);

        if (request.getRoleId() != null) {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, id));
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(id);
            userRole.setRoleId(request.getRoleId());
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setPassword(BCrypt.hashpw("User@123456", BCrypt.gensalt()));
        userMapper.updateById(user);
    }
}
