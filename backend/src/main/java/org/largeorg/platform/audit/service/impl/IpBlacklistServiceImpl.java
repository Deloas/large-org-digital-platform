package org.largeorg.platform.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.audit.dto.BlacklistQueryRequest;
import org.largeorg.platform.audit.dto.BlacklistRequest;
import org.largeorg.platform.audit.entity.IpBlacklist;
import org.largeorg.platform.audit.mapper.IpBlacklistMapper;
import org.largeorg.platform.audit.service.IpBlacklistService;
import org.largeorg.platform.audit.vo.IpBlacklistVo;
import org.largeorg.platform.common.BusinessException;
import org.largeorg.platform.common.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class IpBlacklistServiceImpl implements IpBlacklistService {

    private final IpBlacklistMapper blacklistMapper;

    public IpBlacklistServiceImpl(IpBlacklistMapper blacklistMapper) {
        this.blacklistMapper = blacklistMapper;
    }

    @Override
    public Page<IpBlacklistVo> pageBlacklist(BlacklistQueryRequest request) {
        LambdaQueryWrapper<IpBlacklist> wrapper = new LambdaQueryWrapper<>();
        if (request.getIpAddress() != null && !request.getIpAddress().isBlank()) {
            wrapper.like(IpBlacklist::getIpAddress, request.getIpAddress());
        }
        if (request.getStatus() != null) {
            wrapper.eq(IpBlacklist::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(IpBlacklist::getCreatedAt);

        Page<IpBlacklist> page = blacklistMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()), wrapper);

        Page<IpBlacklistVo> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toVo).toList());
        return result;
    }

    @Override
    public void addBlacklist(BlacklistRequest request, String createdBy) {
        // 检查是否已存在
        Long exists = blacklistMapper.selectCount(
                new LambdaQueryWrapper<IpBlacklist>().eq(IpBlacklist::getIpAddress, request.getIpAddress()));
        if (exists > 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "该 IP 已在黑名单中");
        }

        IpBlacklist entity = new IpBlacklist();
        entity.setIpAddress(request.getIpAddress());
        entity.setReason(request.getReason());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setExpiresAt(request.getExpiresAt());
        entity.setCreatedBy(createdBy);
        blacklistMapper.insert(entity);
    }

    @Override
    public void updateBlacklist(Long id, BlacklistRequest request) {
        IpBlacklist entity = blacklistMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 检查 IP 是否与其他记录冲突
        if (request.getIpAddress() != null && !request.getIpAddress().equals(entity.getIpAddress())) {
            Long exists = blacklistMapper.selectCount(
                    new LambdaQueryWrapper<IpBlacklist>()
                            .eq(IpBlacklist::getIpAddress, request.getIpAddress())
                            .ne(IpBlacklist::getId, id));
            if (exists > 0) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "该 IP 已在黑名单中");
            }
            entity.setIpAddress(request.getIpAddress());
        }
        entity.setReason(request.getReason());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        entity.setExpiresAt(request.getExpiresAt());
        blacklistMapper.updateById(entity);
    }

    @Override
    public void deleteBlacklist(Long id) {
        IpBlacklist entity = blacklistMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        blacklistMapper.deleteById(id);
    }

    private IpBlacklistVo toVo(IpBlacklist entity) {
        return IpBlacklistVo.builder()
                .id(entity.getId())
                .ipAddress(entity.getIpAddress())
                .reason(entity.getReason())
                .status(entity.getStatus())
                .expiresAt(entity.getExpiresAt())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
