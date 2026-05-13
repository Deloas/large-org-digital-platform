package org.largeorg.platform.audit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.audit.dto.LoginLogQueryRequest;
import org.largeorg.platform.audit.dto.OperationLogQueryRequest;
import org.largeorg.platform.audit.vo.LoginLogVo;
import org.largeorg.platform.audit.vo.OperationLogVo;

public interface AuditLogQueryService {
    Page<LoginLogVo> pageLoginLogs(LoginLogQueryRequest request);
    LoginLogVo getLoginLogById(Long id);
    Page<OperationLogVo> pageOperationLogs(OperationLogQueryRequest request);
    OperationLogVo getOperationLogById(Long id);
}
