-- ===================================================
-- 第六阶段：安全审计增强模块 - 增量迁移脚本
-- 包含：安全告警表、IP黑名单表、安全审计增强菜单
-- 执行方式：mysql -u root -p large_org_platform < sql/migration/006_security_audit.sql
-- ===================================================

-- 一、安全告警表
CREATE TABLE IF NOT EXISTS security_alert (
    id              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    alert_type      VARCHAR(32)  NOT NULL COMMENT '告警类型：brute_force / credential_stuffing / off_hours_admin / multi_ip / blacklisted_ip',
    severity        VARCHAR(16)  NOT NULL COMMENT '严重级别：high / medium / low',
    title           VARCHAR(255) NOT NULL COMMENT '告警标题',
    detail          TEXT         COMMENT '告警详情 JSON',
    related_user    VARCHAR(64)  COMMENT '关联用户名',
    related_ip      VARCHAR(45)  COMMENT '关联 IP',
    status          VARCHAR(16)  NOT NULL DEFAULT 'unread' COMMENT 'unread / read / resolved / ignored',
    handler         VARCHAR(64)  COMMENT '处理人',
    handle_note     VARCHAR(512) COMMENT '处理备注',
    duplicate_count INT          DEFAULT 1 COMMENT '重复触发次数',
    first_time      DATETIME     NOT NULL COMMENT '首次检测时间',
    last_time       DATETIME     NOT NULL COMMENT '最近检测时间',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_alert_type (alert_type),
    INDEX idx_severity (severity),
    INDEX idx_status (status),
    INDEX idx_related_user (related_user),
    INDEX idx_related_ip (related_ip),
    INDEX idx_last_time (last_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全告警表';

-- 二、IP 黑名单表
CREATE TABLE IF NOT EXISTS ip_blacklist (
    id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    ip_address   VARCHAR(45)  NOT NULL COMMENT 'IP 地址',
    reason       VARCHAR(255) NOT NULL COMMENT '加黑原因',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
    expires_at   DATETIME     COMMENT '过期时间（null=永不过期）',
    created_by   VARCHAR(64)  COMMENT '创建人',
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_ip_address (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IP 黑名单表';

-- 三、安全审计增强模块菜单（ID 40-49）
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort_order, visible) VALUES
(40, 4, '安全看板', 'menu', '/audit/dashboard',  'audit/SecurityDashboard', 'DataAnalysis', 'audit:dashboard',  0, 1),
(41, 4, '安全告警', 'menu', '/audit/alerts',     'audit/AlertList',         'Warning',      'audit:alert',      3, 1),
(42, 4, 'IP黑名单', 'menu', '/audit/blacklist',  'audit/BlacklistList',     'VideoCamera',  'audit:blacklist',  4, 1);

-- 四、admin 角色获得安全审计增强模块全部菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 40),
(1, 41),
(1, 42);

-- 五、auditor 角色获得安全审计增强模块全部菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(6, 40),
(6, 41),
(6, 42);
