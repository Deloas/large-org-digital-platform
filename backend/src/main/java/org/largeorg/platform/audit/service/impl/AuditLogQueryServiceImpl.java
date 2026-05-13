package org.largeorg.platform.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.audit.dto.LoginLogQueryRequest;
import org.largeorg.platform.audit.dto.OperationLogQueryRequest;
import org.largeorg.platform.audit.entity.AuditLoginLog;
import org.largeorg.platform.audit.entity.AuditOperationLog;
import org.largeorg.platform.audit.mapper.AuditLoginLogMapper;
import org.largeorg.platform.audit.mapper.AuditOperationLogMapper;
import org.largeorg.platform.audit.service.AuditLogQueryService;
import org.largeorg.platform.audit.vo.LoginLogVo;
import org.largeorg.platform.audit.vo.OperationLogVo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AuditLogQueryServiceImpl implements AuditLogQueryService {

    private final AuditLoginLogMapper loginLogMapper;
    private final AuditOperationLogMapper operationLogMapper;

    public AuditLogQueryServiceImpl(AuditLoginLogMapper loginLogMapper,
                                    AuditOperationLogMapper operationLogMapper) {
        this.loginLogMapper = loginLogMapper;
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public Page<LoginLogVo> pageLoginLogs(LoginLogQueryRequest request) {
        LambdaQueryWrapper<AuditLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            wrapper.like(AuditLoginLog::getUsername, request.getUsername());
        }
        if (request.getIp() != null && !request.getIp().isBlank()) {
            wrapper.like(AuditLoginLog::getLoginIp, request.getIp());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            wrapper.eq(AuditLoginLog::getStatus, request.getStatus());
        }
        if (request.getStartTime() != null && !request.getStartTime().isBlank()) {
            wrapper.ge(AuditLoginLog::getLoginTime, parseDateTime(request.getStartTime()));
        }
        if (request.getEndTime() != null && !request.getEndTime().isBlank()) {
            wrapper.le(AuditLoginLog::getLoginTime, parseDateTime(request.getEndTime()));
        }
        wrapper.orderByDesc(AuditLoginLog::getLoginTime);

        Page<AuditLoginLog> page = loginLogMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()), wrapper);

        Page<LoginLogVo> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toLoginLogVo).toList());
        return result;
    }

    @Override
    public LoginLogVo getLoginLogById(Long id) {
        AuditLoginLog entity = loginLogMapper.selectById(id);
        return entity == null ? null : toLoginLogVo(entity);
    }

    @Override
    public Page<OperationLogVo> pageOperationLogs(OperationLogQueryRequest request) {
        LambdaQueryWrapper<AuditOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            wrapper.like(AuditOperationLog::getUsername, request.getUsername());
        }
        if (request.getModule() != null && !request.getModule().isBlank()) {
            wrapper.eq(AuditOperationLog::getModule, request.getModule());
        }
        if (request.getAction() != null && !request.getAction().isBlank()) {
            wrapper.like(AuditOperationLog::getAction, request.getAction());
        }
        if (request.getRequestPath() != null && !request.getRequestPath().isBlank()) {
            wrapper.like(AuditOperationLog::getRequestPath, request.getRequestPath());
        }
        if (request.getResult() != null && !request.getResult().isBlank()) {
            wrapper.eq(AuditOperationLog::getResult, request.getResult());
        }
        if (request.getStartTime() != null && !request.getStartTime().isBlank()) {
            wrapper.ge(AuditOperationLog::getCreatedAt, parseDateTime(request.getStartTime()));
        }
        if (request.getEndTime() != null && !request.getEndTime().isBlank()) {
            wrapper.le(AuditOperationLog::getCreatedAt, parseDateTime(request.getEndTime()));
        }
        wrapper.orderByDesc(AuditOperationLog::getCreatedAt);

        Page<AuditOperationLog> page = operationLogMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()), wrapper);

        Page<OperationLogVo> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toOperationLogVo).toList());
        return result;
    }

    @Override
    public OperationLogVo getOperationLogById(Long id) {
        AuditOperationLog entity = operationLogMapper.selectById(id);
        return entity == null ? null : toOperationLogVo(entity);
    }

    private LoginLogVo toLoginLogVo(AuditLoginLog entity) {
        return LoginLogVo.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .loginIp(entity.getLoginIp())
                .userAgent(entity.getUserAgent())
                .status(entity.getStatus())
                .failReason(entity.getFailReason())
                .loginTime(entity.getLoginTime())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private OperationLogVo toOperationLogVo(AuditOperationLog entity) {
        return OperationLogVo.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .module(entity.getModule())
                .action(entity.getAction())
                .requestPath(entity.getRequestPath())
                .requestMethod(entity.getRequestMethod())
                .requestParams(entity.getRequestParams())
                .result(entity.getResult())
                .errorMsg(entity.getErrorMsg())
                .costMs(entity.getCostMs())
                .ip(entity.getIp())
                .userAgent(entity.getUserAgent())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private LocalDateTime parseDateTime(String str) {
        try {
            return LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
