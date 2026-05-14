package org.largeorg.platform.audit.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SecurityDashboardVo {
    private long totalAlertsUnread;
    private long highAlertsToday;
    private long totalAlertsToday;
    private long blacklistCount;
    private List<Map<String, Object>> alertTrend;
    private List<Map<String, Object>> alertByType;
    private List<Map<String, Object>> alertBySeverity;
    private List<SecurityAlertVo> recentHighAlerts;
    private List<Map<String, Object>> topFailedAccounts;
}
