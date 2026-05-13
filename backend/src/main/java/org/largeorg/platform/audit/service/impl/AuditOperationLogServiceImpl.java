package org.largeorg.platform.audit.service.impl;

import org.largeorg.platform.audit.entity.AuditOperationLog;
import org.largeorg.platform.audit.mapper.AuditOperationLogMapper;
import org.largeorg.platform.audit.service.AuditOperationLogService;
import org.springframework.stereotype.Service;

@Service
public class AuditOperationLogServiceImpl implements AuditOperationLogService {

    private final AuditOperationLogMapper operationLogMapper;

    public AuditOperationLogServiceImpl(AuditOperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public void save(AuditOperationLog log) {
        try {
            operationLogMapper.insert(log);
        } catch (Exception ignored) {
            // 日志写入失败不影响业务主流程
        }
    }
}
