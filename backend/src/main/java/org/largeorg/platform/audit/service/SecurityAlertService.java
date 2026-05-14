package org.largeorg.platform.audit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.audit.dto.AlertQueryRequest;
import org.largeorg.platform.audit.vo.SecurityAlertVo;
import org.largeorg.platform.audit.vo.SecurityDashboardVo;

public interface SecurityAlertService {
    Page<SecurityAlertVo> pageAlerts(AlertQueryRequest request);
    SecurityAlertVo getAlertById(Long id);
    void updateAlertStatus(Long id, String status, String note, String handler);
    SecurityDashboardVo getDashboard();
}
