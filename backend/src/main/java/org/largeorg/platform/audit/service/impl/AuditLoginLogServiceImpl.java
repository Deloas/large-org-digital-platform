package org.largeorg.platform.audit.service.impl;

import org.largeorg.platform.audit.entity.AuditLoginLog;
import org.largeorg.platform.audit.mapper.AuditLoginLogMapper;
import org.largeorg.platform.audit.service.AuditLoginLogService;
import org.springframework.stereotype.Service;

@Service
public class AuditLoginLogServiceImpl implements AuditLoginLogService {

    private final AuditLoginLogMapper loginLogMapper;

    public AuditLoginLogServiceImpl(AuditLoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    public void save(AuditLoginLog log) {
        try {
            loginLogMapper.insert(log);
        } catch (Exception ignored) {
            // 日志写入失败不影响业务主流程
        }
    }
}
