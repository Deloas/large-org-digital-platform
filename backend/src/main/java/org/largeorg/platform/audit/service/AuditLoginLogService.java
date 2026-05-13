package org.largeorg.platform.audit.service;

import org.largeorg.platform.audit.entity.AuditLoginLog;

public interface AuditLoginLogService {
    void save(AuditLoginLog log);
}
