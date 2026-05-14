package org.largeorg.platform.audit.constant;

public final class SecurityConstants {

    private SecurityConstants() {}

    // 告警类型
    public static final String ALERT_TYPE_BRUTE_FORCE = "brute_force";
    public static final String ALERT_TYPE_CREDENTIAL_STUFFING = "credential_stuffing";
    public static final String ALERT_TYPE_OFF_HOURS_ADMIN = "off_hours_admin";
    public static final String ALERT_TYPE_MULTI_IP = "multi_ip";
    public static final String ALERT_TYPE_BLACKLISTED_IP = "blacklisted_ip";

    // 严重级别
    public static final String SEVERITY_HIGH = "high";
    public static final String SEVERITY_MEDIUM = "medium";
    public static final String SEVERITY_LOW = "low";

    // 告警状态
    public static final String STATUS_UNREAD = "unread";
    public static final String STATUS_READ = "read";
    public static final String STATUS_RESOLVED = "resolved";
    public static final String STATUS_IGNORED = "ignored";

    // 检测窗口（分钟）
    public static final int BRUTE_FORCE_WINDOW_MINUTES = 5;
    public static final int CREDENTIAL_STUFFING_WINDOW_MINUTES = 5;
    public static final int OFF_HOURS_WINDOW_MINUTES = 5;
    public static final int MULTI_IP_WINDOW_MINUTES = 10;
    public static final int BLACKLISTED_IP_WINDOW_MINUTES = 5;

    // 检测阈值
    public static final int BRUTE_FORCE_THRESHOLD = 5;
    public static final int CREDENTIAL_STUFFING_THRESHOLD = 3;
    public static final int MULTI_IP_THRESHOLD = 3;

    // 工作时间（小时）
    public static final int WORK_HOUR_START = 8;
    public static final int WORK_HOUR_END = 18;

    // 去重窗口（分钟）
    public static final int DEDUP_WINDOW_MINUTES = 30;

    // 调度间隔（毫秒）
    public static final long DETECTION_INTERVAL_MS = 300_000;
}
