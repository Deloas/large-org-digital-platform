package org.largeorg.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.largeorg.platform.common.BusinessException;
import org.largeorg.platform.common.ErrorCode;
import org.largeorg.platform.system.dto.RoleCreateRequest;
import org.largeorg.platform.system.dto.RoleUpdateRequest;
import org.largeorg.platform.system.entity.SysRole;
import org.largeorg.platform.system.entity.SysRoleMenu;
import org.largeorg.platform.system.mapper.RoleMapper;
import org.largeorg.platform.system.mapper.RoleMenuMapper;
import org.largeorg.platform.system.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;

    public RoleServiceImpl(RoleMapper roleMapper, RoleMenuMapper roleMenuMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public List<SysRole> list() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .orderByAsc(SysRole::getSortOrder));
    }

    @Override
    public SysRole getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public void create(RoleCreateRequest request) {
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, request.getRoleCode()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色编码已存在");
        }
        SysRole role = new SysRole();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        role.setStatus(1);
        roleMapper.insert(role);
    }

    @Override
    public void update(Long id, RoleUpdateRequest request) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        if (request.getRoleName() != null) role.setRoleName(request.getRoleName());
        if (request.getDescription() != null) role.setDescription(request.getDescription());
        if (request.getSortOrder() != null) role.setSortOrder(request.getSortOrder());
        roleMapper.updateById(role);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        roleMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
    }

    @Override
    public List<Long> getMenuIds(Long roleId) {
        return roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
    }
}
