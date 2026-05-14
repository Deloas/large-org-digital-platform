package org.largeorg.platform.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.audit.constant.SecurityConstants;
import org.largeorg.platform.audit.dto.AlertQueryRequest;
import org.largeorg.platform.audit.entity.AuditLoginLog;
import org.largeorg.platform.audit.entity.IpBlacklist;
import org.largeorg.platform.audit.entity.SecurityAlert;
import org.largeorg.platform.audit.mapper.AuditLoginLogMapper;
import org.largeorg.platform.audit.mapper.IpBlacklistMapper;
import org.largeorg.platform.audit.mapper.SecurityAlertMapper;
import org.largeorg.platform.audit.service.SecurityAlertService;
import org.largeorg.platform.audit.vo.SecurityAlertVo;
import org.largeorg.platform.audit.vo.SecurityDashboardVo;
import org.largeorg.platform.common.BusinessException;
import org.largeorg.platform.common.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SecurityAlertServiceImpl implements SecurityAlertService {

    private final SecurityAlertMapper alertMapper;
    private final IpBlacklistMapper blacklistMapper;
    private final AuditLoginLogMapper loginLogMapper;

    public SecurityAlertServiceImpl(SecurityAlertMapper alertMapper,
                                     IpBlacklistMapper blacklistMapper,
                                     AuditLoginLogMapper loginLogMapper) {
        this.alertMapper = alertMapper;
        this.blacklistMapper = blacklistMapper;
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    public Page<SecurityAlertVo> pageAlerts(AlertQueryRequest request) {
        LambdaQueryWrapper<SecurityAlert> wrapper = new LambdaQueryWrapper<>();
        if (request.getAlertType() != null && !request.getAlertType().isBlank()) {
            wrapper.eq(SecurityAlert::getAlertType, request.getAlertType());
        }
        if (request.getSeverity() != null && !request.getSeverity().isBlank()) {
            wrapper.eq(SecurityAlert::getSeverity, request.getSeverity());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            wrapper.eq(SecurityAlert::getStatus, request.getStatus());
        }
        if (request.getRelatedUser() != null && !request.getRelatedUser().isBlank()) {
            wrapper.like(SecurityAlert::getRelatedUser, request.getRelatedUser());
        }
        if (request.getRelatedIp() != null && !request.getRelatedIp().isBlank()) {
            wrapper.like(SecurityAlert::getRelatedIp, request.getRelatedIp());
        }
        if (request.getStartTime() != null && !request.getStartTime().isBlank()) {
            wrapper.ge(SecurityAlert::getLastTime, parseDateTime(request.getStartTime()));
        }
        if (request.getEndTime() != null && !request.getEndTime().isBlank()) {
            wrapper.le(SecurityAlert::getLastTime, parseDateTime(request.getEndTime()));
        }
        wrapper.orderByDesc(SecurityAlert::getLastTime);

        Page<SecurityAlert> page = alertMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()), wrapper);

        Page<SecurityAlertVo> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toVo).toList());
        return result;
    }

    @Override
    public SecurityAlertVo getAlertById(Long id) {
        SecurityAlert entity = alertMapper.selectById(id);
        return entity == null ? null : toVo(entity);
    }

    @Override
    public void updateAlertStatus(Long id, String status, String note, String handler) {
        SecurityAlert alert = alertMapper.selectById(id);
        if (alert == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        alert.setStatus(status);
        alert.setHandler(handler);
        if (note != null && !note.isBlank()) {
            alert.setHandleNote(note);
        }
        alertMapper.updateById(alert);
    }

    public SecurityDashboardVo getDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime sevenDaysAgo = todayStart.minusDays(6);

        // 未读告警数
        long totalAlertsUnread = alertMapper.selectCount(
                new LambdaQueryWrapper<SecurityAlert>().eq(SecurityAlert::getStatus, SecurityConstants.STATUS_UNREAD));

        // 今日高危告警数
        long highAlertsToday = alertMapper.selectCount(
                new LambdaQueryWrapper<SecurityAlert>()
                        .eq(SecurityAlert::getSeverity, SecurityConstants.SEVERITY_HIGH)
                        .ge(SecurityAlert::getCreatedAt, todayStart));

        // 今日告警总数
        long totalAlertsToday = alertMapper.selectCount(
                new LambdaQueryWrapper<SecurityAlert>()
                        .ge(SecurityAlert::getCreatedAt, todayStart));

        // 黑名单 IP 数
        long blacklistCount = blacklistMapper.selectCount(
                new LambdaQueryWrapper<IpBlacklist>().eq(IpBlacklist::getStatus, 1));

        // 近 7 天告警趋势
        List<Map<String, Object>> alertTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            long count = alertMapper.selectCount(
                    new LambdaQueryWrapper<SecurityAlert>()
                            .ge(SecurityAlert::getCreatedAt, dayStart)
                            .le(SecurityAlert::getCreatedAt, dayEnd));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
            item.put("count", count);
            alertTrend.add(item);
        }

        // 告警类型分布（未忽略/未处理）
        List<SecurityAlert> activeAlerts = alertMapper.selectList(
                new LambdaQueryWrapper<SecurityAlert>()
                        .notIn(SecurityAlert::getStatus, SecurityConstants.STATUS_RESOLVED, SecurityConstants.STATUS_IGNORED));
        Map<String, Long> typeCount = activeAlerts.stream()
                .collect(Collectors.groupingBy(SecurityAlert::getAlertType, Collectors.counting()));
        List<Map<String, Object>> alertByType = typeCount.entrySet().stream().map(e -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("alertType", e.getKey());
            item.put("count", e.getValue());
            return item;
        }).collect(Collectors.toList());

        // 告警严重级别分布
        Map<String, Long> severityCount = activeAlerts.stream()
                .collect(Collectors.groupingBy(SecurityAlert::getSeverity, Collectors.counting()));
        List<Map<String, Object>> alertBySeverity = severityCount.entrySet().stream().map(e -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("severity", e.getKey());
            item.put("count", e.getValue());
            return item;
        }).collect(Collectors.toList());

        // 最近高危告警
        List<SecurityAlert> recentHigh = alertMapper.selectList(
                new LambdaQueryWrapper<SecurityAlert>()
                        .eq(SecurityAlert::getSeverity, SecurityConstants.SEVERITY_HIGH)
                        .orderByDesc(SecurityAlert::getLastTime)
                        .last("LIMIT 10"));
        List<SecurityAlertVo> recentHighAlerts = recentHigh.stream().map(this::toVo).toList();

        // 近 24h 登录失败 Top 5 账号
        LocalDateTime yesterday = now.minusHours(24);
        List<AuditLoginLog> failLogs = loginLogMapper.selectList(
                new LambdaQueryWrapper<AuditLoginLog>()
                        .eq(AuditLoginLog::getStatus, "fail")
                        .ge(AuditLoginLog::getLoginTime, yesterday));
        Map<String, Long> failCountByUser = failLogs.stream()
                .collect(Collectors.groupingBy(AuditLoginLog::getUsername, Collectors.counting()));
        List<Map<String, Object>> topFailedAccounts = failCountByUser.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("username", e.getKey());
                    item.put("count", e.getValue());
                    return item;
                }).collect(Collectors.toList());

        return SecurityDashboardVo.builder()
                .totalAlertsUnread(totalAlertsUnread)
                .highAlertsToday(highAlertsToday)
                .totalAlertsToday(totalAlertsToday)
                .blacklistCount(blacklistCount)
                .alertTrend(alertTrend)
                .alertByType(alertByType)
                .alertBySeverity(alertBySeverity)
                .recentHighAlerts(recentHighAlerts)
                .topFailedAccounts(topFailedAccounts)
                .build();
    }

    private SecurityAlertVo toVo(SecurityAlert entity) {
        return SecurityAlertVo.builder()
                .id(entity.getId())
                .alertType(entity.getAlertType())
                .severity(entity.getSeverity())
                .title(entity.getTitle())
                .detail(entity.getDetail())
                .relatedUser(entity.getRelatedUser())
                .relatedIp(entity.getRelatedIp())
                .status(entity.getStatus())
                .handler(entity.getHandler())
                .handleNote(entity.getHandleNote())
                .duplicateCount(entity.getDuplicateCount())
                .firstTime(entity.getFirstTime())
                .lastTime(entity.getLastTime())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
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
