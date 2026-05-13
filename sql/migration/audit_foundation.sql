-- ===================================================
-- 第三阶段：日志审计基础能力 - 增量迁移脚本
-- 适用于已有第二阶段数据的数据库升级
-- 执行方式：mysql -u root -p large_org_platform < sql/migration/audit_foundation.sql
-- ===================================================

-- 一、创建日志表（IF NOT EXISTS 保证幂等）
CREATE TABLE IF NOT EXISTS audit_login_log (
    id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT       COMMENT '用户ID（登录失败时可能为null）',
    username     VARCHAR(64)  NOT NULL COMMENT '用户名',
    login_ip     VARCHAR(45)  COMMENT '登录IP',
    user_agent   VARCHAR(512) COMMENT 'User-Agent',
    status       VARCHAR(16)  NOT NULL COMMENT 'success / fail',
    fail_reason  VARCHAR(255) COMMENT '失败原因',
    login_time   DATETIME     NOT NULL COMMENT '登录时间',
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    INDEX idx_username (username),
    INDEX idx_status (status),
    INDEX idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

CREATE TABLE IF NOT EXISTS audit_operation_log (
    id             BIGINT       PRIMARY KEY AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL COMMENT '操作人ID',
    username       VARCHAR(64)  NOT NULL COMMENT '操作人用户名',
    module         VARCHAR(64)  NOT NULL COMMENT '操作模块',
    action         VARCHAR(64)  NOT NULL COMMENT '操作动作',
    request_path   VARCHAR(255) COMMENT '请求路径',
    request_method VARCHAR(10)  COMMENT '请求方法',
    request_params TEXT         COMMENT '请求参数摘要',
    result         VARCHAR(16)  NOT NULL COMMENT 'success / fail',
    error_msg      VARCHAR(512) COMMENT '异常信息摘要',
    cost_ms        BIGINT       COMMENT '耗时（毫秒）',
    ip             VARCHAR(45)  COMMENT '操作IP',
    user_agent     VARCHAR(512) COMMENT 'User-Agent',
    created_at     DATETIME     NOT NULL COMMENT '操作时间',
    INDEX idx_username (username),
    INDEX idx_module (module),
    INDEX idx_result (result),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 二、新增安全审计子菜单（INSERT IGNORE 保证幂等）
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort_order, visible) VALUES
(10, 4, '登录日志', 'menu', '/audit/login-logs', 'audit/LoginLogList', 'Notebook', 'audit:log', 1, 1),
(11, 4, '操作日志', 'menu', '/audit/operation-logs', 'audit/OperationLogList', 'Tickets', 'audit:log', 2, 1);

-- 三、给 auditor 角色添加安全审计菜单授权（INSERT IGNORE 保证幂等）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(6, 4),
(6, 10),
(6, 11);

-- 四、admin 角色获取新增菜单（INSERT IGNORE 保证幂等）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 10),
(1, 11);
