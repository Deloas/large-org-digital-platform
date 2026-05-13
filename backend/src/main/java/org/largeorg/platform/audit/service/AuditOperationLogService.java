package org.largeorg.platform.audit.service;

import org.largeorg.platform.audit.entity.AuditOperationLog;

public interface AuditOperationLogService {
    void save(AuditOperationLog log);
}
