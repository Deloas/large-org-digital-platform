package org.largeorg.platform.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.largeorg.platform.audit.constant.SecurityConstants;
import org.largeorg.platform.audit.entity.AuditLoginLog;
import org.largeorg.platform.audit.entity.IpBlacklist;
import org.largeorg.platform.audit.entity.SecurityAlert;
import org.largeorg.platform.audit.mapper.AuditLoginLogMapper;
import org.largeorg.platform.audit.mapper.IpBlacklistMapper;
import org.largeorg.platform.audit.mapper.SecurityAlertMapper;
import org.largeorg.platform.audit.service.SecurityDetectionService;
import org.largeorg.platform.system.entity.SysRole;
import org.largeorg.platform.system.entity.SysUserRole;
import org.largeorg.platform.system.mapper.RoleMapper;
import org.largeorg.platform.system.mapper.UserRoleMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SecurityDetectionServiceImpl implements SecurityDetectionService {

    private final AuditLoginLogMapper loginLogMapper;
    private final SecurityAlertMapper alertMapper;
    private final IpBlacklistMapper blacklistMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    public SecurityDetectionServiceImpl(AuditLoginLogMapper loginLogMapper,
                                        SecurityAlertMapper alertMapper,
                                        IpBlacklistMapper blacklistMapper,
                                        UserRoleMapper userRoleMapper,
                                        RoleMapper roleMapper) {
        this.loginLogMapper = loginLogMapper;
        this.alertMapper = alertMapper;
        this.blacklistMapper = blacklistMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public void runAllChecks() {
        try {
            checkBruteForce();
        } catch (Exception ignored) {}
        try {
            checkCredentialStuffing();
        } catch (Exception ignored) {}
        try {
            checkOffHoursAdmin();
        } catch (Exception ignored) {}
        try {
            checkMultiIp();
        } catch (Exception ignored) {}
        try {
            checkBlacklistedIp();
        } catch (Exception ignored) {}
    }

    // R1: 暴力破解检测
    private void checkBruteForce() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(SecurityConstants.BRUTE_FORCE_WINDOW_MINUTES);
        List<AuditLoginLog> logs = loginLogMapper.selectList(
                new LambdaQueryWrapper<AuditLoginLog>()
                        .eq(AuditLoginLog::getStatus, "fail")
                        .ge(AuditLoginLog::getLoginTime, since));

        Map<String, Long> countByUser = logs.stream()
                .collect(Collectors.groupingBy(AuditLoginLog::getUsername, Collectors.counting()));

        for (Map.Entry<String, Long> entry : countByUser.entrySet()) {
            if (entry.getValue() > SecurityConstants.BRUTE_FORCE_THRESHOLD) {
                String username = entry.getKey();
                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("failCount", entry.getValue());
                detail.put("windowMinutes", SecurityConstants.BRUTE_FORCE_WINDOW_MINUTES);

                dedupOrCreate(SecurityConstants.ALERT_TYPE_BRUTE_FORCE,
                        SecurityConstants.SEVERITY_HIGH,
                        "暴力破解告警：" + username,
                        detail,
                        username, null);
            }
        }
    }

    // R2: 撞库风险检测
    private void checkCredentialStuffing() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(SecurityConstants.CREDENTIAL_STUFFING_WINDOW_MINUTES);
        List<AuditLoginLog> logs = loginLogMapper.selectList(
                new LambdaQueryWrapper<AuditLoginLog>()
                        .ge(AuditLoginLog::getLoginTime, since));

        Map<String, Set<String>> usersByIp = new LinkedHashMap<>();
        for (AuditLoginLog log : logs) {
            String ip = log.getLoginIp();
            if (ip == null || ip.isBlank()) continue;
            usersByIp.computeIfAbsent(ip, k -> new HashSet<>()).add(log.getUsername());
        }

        for (Map.Entry<String, Set<String>> entry : usersByIp.entrySet()) {
            if (entry.getValue().size() >= SecurityConstants.CREDENTIAL_STUFFING_THRESHOLD) {
                String ip = entry.getKey();
                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("distinctAccounts", entry.getValue().size());
                detail.put("windowMinutes", SecurityConstants.CREDENTIAL_STUFFING_WINDOW_MINUTES);
                detail.put("accounts", new ArrayList<>(entry.getValue()));

                dedupOrCreate(SecurityConstants.ALERT_TYPE_CREDENTIAL_STUFFING,
                        SecurityConstants.SEVERITY_HIGH,
                        "撞库风险告警：IP " + ip,
                        detail,
                        null, ip);
            }
        }
    }

    // R3: 非工作时间管理员登录检测
    private void checkOffHoursAdmin() {
        Set<Long> adminUserIds = getAdminUserIds();
        if (adminUserIds.isEmpty()) return;

        LocalDateTime since = LocalDateTime.now().minusMinutes(SecurityConstants.OFF_HOURS_WINDOW_MINUTES);
        int currentHour = LocalDateTime.now().getHour();

        // 当前时间在工作时间内则跳过（非工作时间才需要检测）
        if (currentHour >= SecurityConstants.WORK_HOUR_START && currentHour < SecurityConstants.WORK_HOUR_END) {
            return;
        }

        List<AuditLoginLog> logs = loginLogMapper.selectList(
                new LambdaQueryWrapper<AuditLoginLog>()
                        .eq(AuditLoginLog::getStatus, "success")
                        .ge(AuditLoginLog::getLoginTime, since));

        for (AuditLoginLog log : logs) {
            if (log.getUserId() == null || !adminUserIds.contains(log.getUserId())) continue;

            int loginHour = log.getLoginTime().getHour();
            if (loginHour >= SecurityConstants.WORK_HOUR_START && loginHour < SecurityConstants.WORK_HOUR_END) {
                continue;
            }

            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("loginTime", log.getLoginTime().toString());
            detail.put("loginIp", log.getLoginIp());
            detail.put("userId", log.getUserId());

            dedupOrCreate(SecurityConstants.ALERT_TYPE_OFF_HOURS_ADMIN,
                    SecurityConstants.SEVERITY_MEDIUM,
                    "非工作时间管理员登录：" + log.getUsername(),
                    detail,
                    log.getUsername(), log.getLoginIp());
        }
    }

    // R4: 多 IP 登录检测
    private void checkMultiIp() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(SecurityConstants.MULTI_IP_WINDOW_MINUTES);
        List<AuditLoginLog> logs = loginLogMapper.selectList(
                new LambdaQueryWrapper<AuditLoginLog>()
                        .eq(AuditLoginLog::getStatus, "success")
                        .ge(AuditLoginLog::getLoginTime, since));

        Map<String, Set<String>> ipsByUser = new LinkedHashMap<>();
        for (AuditLoginLog log : logs) {
            String ip = log.getLoginIp();
            if (ip == null || ip.isBlank()) continue;
            ipsByUser.computeIfAbsent(log.getUsername(), k -> new HashSet<>()).add(ip);
        }

        for (Map.Entry<String, Set<String>> entry : ipsByUser.entrySet()) {
            if (entry.getValue().size() >= SecurityConstants.MULTI_IP_THRESHOLD) {
                String username = entry.getKey();
                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("distinctIps", entry.getValue().size());
                detail.put("windowMinutes", SecurityConstants.MULTI_IP_WINDOW_MINUTES);
                detail.put("ips", new ArrayList<>(entry.getValue()));

                dedupOrCreate(SecurityConstants.ALERT_TYPE_MULTI_IP,
                        SecurityConstants.SEVERITY_MEDIUM,
                        "账号异常告警：" + username,
                        detail,
                        username, null);
            }
        }
    }

    // R5: 黑名单 IP 登录检测
    private void checkBlacklistedIp() {
        // 获取所有启用的黑名单 IP（未过期）
        List<IpBlacklist> activeBlacklist = blacklistMapper.selectList(
                new LambdaQueryWrapper<IpBlacklist>()
                        .eq(IpBlacklist::getStatus, 1)
                        .and(w -> w.isNull(IpBlacklist::getExpiresAt).or().gt(IpBlacklist::getExpiresAt, LocalDateTime.now())));

        if (activeBlacklist.isEmpty()) return;

        Set<String> blacklistedIps = activeBlacklist.stream()
                .map(IpBlacklist::getIpAddress)
                .collect(Collectors.toSet());

        LocalDateTime since = LocalDateTime.now().minusMinutes(SecurityConstants.BLACKLISTED_IP_WINDOW_MINUTES);
        List<AuditLoginLog> logs = loginLogMapper.selectList(
                new LambdaQueryWrapper<AuditLoginLog>()
                        .eq(AuditLoginLog::getStatus, "success")
                        .ge(AuditLoginLog::getLoginTime, since)
                        .in(AuditLoginLog::getLoginIp, blacklistedIps));

        for (AuditLoginLog log : logs) {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("loginTime", log.getLoginTime().toString());
            detail.put("username", log.getUsername());
            detail.put("userId", log.getUserId());

            dedupOrCreate(SecurityConstants.ALERT_TYPE_BLACKLISTED_IP,
                    SecurityConstants.SEVERITY_HIGH,
                    "黑名单 IP 登录告警：" + log.getLoginIp(),
                    detail,
                    null, log.getLoginIp());
        }
    }

    // 获取管理员用户 ID 集合
    private Set<Long> getAdminUserIds() {
        List<SysRole> adminRoles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, "admin")
                        .eq(SysRole::getStatus, 1));
        if (adminRoles.isEmpty()) return Collections.emptySet();

        Set<Long> adminRoleIds = adminRoles.stream().map(SysRole::getId).collect(Collectors.toSet());
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .in(SysUserRole::getRoleId, adminRoleIds));
        return userRoles.stream().map(SysUserRole::getUserId).collect(Collectors.toSet());
    }

    // 去重或创建告警
    private void dedupOrCreate(String alertType, String severity, String title,
                                Map<String, Object> detail, String relatedUser, String relatedIp) {
        LocalDateTime dedupSince = LocalDateTime.now().minusMinutes(SecurityConstants.DEDUP_WINDOW_MINUTES);

        LambdaQueryWrapper<SecurityAlert> wrapper = new LambdaQueryWrapper<SecurityAlert>()
                .eq(SecurityAlert::getAlertType, alertType)
                .in(SecurityAlert::getStatus, SecurityConstants.STATUS_UNREAD, SecurityConstants.STATUS_READ)
                .ge(SecurityAlert::getLastTime, dedupSince);

        if (relatedUser != null) {
            wrapper.eq(SecurityAlert::getRelatedUser, relatedUser);
        }
        if (relatedIp != null) {
            wrapper.eq(SecurityAlert::getRelatedIp, relatedIp);
        }

        SecurityAlert existing = alertMapper.selectOne(wrapper, false);

        if (existing != null) {
            existing.setDuplicateCount(existing.getDuplicateCount() + 1);
            existing.setLastTime(LocalDateTime.now());
            String existingDetail = existing.getDetail();
            if (existingDetail == null || existingDetail.isBlank()) {
                existing.setDetail(toJson(detail));
            }
            alertMapper.updateById(existing);
        } else {
            SecurityAlert alert = new SecurityAlert();
            alert.setAlertType(alertType);
            alert.setSeverity(severity);
            alert.setTitle(title);
            alert.setDetail(toJson(detail));
            alert.setRelatedUser(relatedUser);
            alert.setRelatedIp(relatedIp);
            alert.setStatus(SecurityConstants.STATUS_UNREAD);
            alert.setDuplicateCount(1);
            alert.setFirstTime(LocalDateTime.now());
            alert.setLastTime(LocalDateTime.now());
            alertMapper.insert(alert);
        }
    }

    private String toJson(Map<String, Object> detail) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : detail.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            Object val = entry.getValue();
            if (val instanceof String) {
                sb.append("\"").append(val).append("\"");
            } else if (val instanceof List) {
                sb.append("[");
                List<?> list = (List<?>) val;
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append("\"").append(list.get(i)).append("\"");
                }
                sb.append("]");
            } else {
                sb.append(val);
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
