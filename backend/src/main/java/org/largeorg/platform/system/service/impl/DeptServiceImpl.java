package org.largeorg.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.largeorg.platform.common.BusinessException;
import org.largeorg.platform.common.ErrorCode;
import org.largeorg.platform.system.dto.DeptCreateRequest;
import org.largeorg.platform.system.dto.DeptUpdateRequest;
import org.largeorg.platform.system.entity.SysDept;
import org.largeorg.platform.system.mapper.DeptMapper;
import org.largeorg.platform.system.service.DeptService;
import org.largeorg.platform.system.vo.DeptVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;

    public DeptServiceImpl(DeptMapper deptMapper) {
        this.deptMapper = deptMapper;
    }

    @Override
    public List<DeptVo> tree() {
        List<SysDept> all = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .orderByAsc(SysDept::getSortOrder));
        List<DeptVo> voList = all.stream().map(d -> {
            DeptVo vo = new DeptVo();
            vo.setId(d.getId());
            vo.setDeptName(d.getDeptName());
            vo.setParentId(d.getParentId());
            vo.setLeaderName(d.getLeaderName());
            vo.setPhone(d.getPhone());
            vo.setSortOrder(d.getSortOrder());
            vo.setStatus(d.getStatus());
            vo.setCreatedAt(d.getCreatedAt());
            vo.setChildren(new ArrayList<>());
            return vo;
        }).collect(Collectors.toList());

        Map<Long, DeptVo> map = voList.stream()
                .collect(Collectors.toMap(DeptVo::getId, v -> v));
        List<DeptVo> roots = new ArrayList<>();
        for (DeptVo vo : voList) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                roots.add(vo);
            } else {
                DeptVo parent = map.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }
        return roots;
    }

    @Override
    public SysDept getById(Long id) {
        return deptMapper.selectById(id);
    }

    @Override
    public void create(DeptCreateRequest request) {
        SysDept dept = new SysDept();
        dept.setDeptName(request.getDeptName());
        dept.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        dept.setLeaderName(request.getLeaderName());
        dept.setPhone(request.getPhone());
        dept.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        dept.setStatus(1);
        deptMapper.insert(dept);
    }

    @Override
    public void update(Long id, DeptUpdateRequest request) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "部门不存在");
        }
        if (request.getDeptName() != null) dept.setDeptName(request.getDeptName());
        if (request.getParentId() != null) dept.setParentId(request.getParentId());
        if (request.getLeaderName() != null) dept.setLeaderName(request.getLeaderName());
        if (request.getPhone() != null) dept.setPhone(request.getPhone());
        if (request.getSortOrder() != null) dept.setSortOrder(request.getSortOrder());
        deptMapper.updateById(dept);
    }

    @Override
    public void delete(Long id) {
        Long childCount = deptMapper.selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "存在子部门，无法删除");
        }
        deptMapper.deleteById(id);
    }
}
