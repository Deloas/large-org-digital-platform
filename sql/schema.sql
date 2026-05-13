-- ===================================================
-- 大型组织数字化办公与安全审计一体化平台
-- 第二阶段：认证与权限模块 - 建表脚本
-- ===================================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    real_name    VARCHAR(50),
    email        VARCHAR(100),
    phone        VARCHAR(20),
    dept_id      BIGINT,
    avatar       VARCHAR(255),
    status       TINYINT      NOT NULL DEFAULT 1  COMMENT '1=启用 0=禁用',
    deleted      TINYINT      NOT NULL DEFAULT 0  COMMENT '逻辑删除',
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_dept_id (dept_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    role_code    VARCHAR(50)  NOT NULL UNIQUE COMMENT '角色编码',
    role_name    VARCHAR(100) NOT NULL,
    description  VARCHAR(255),
    sort_order   INT          DEFAULT 0,
    status       TINYINT      NOT NULL DEFAULT 1  COMMENT '1=启用 0=禁用',
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id  BIGINT NOT NULL,
    role_id  BIGINT NOT NULL,
    UNIQUE INDEX uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    dept_name    VARCHAR(100) NOT NULL,
    parent_id    BIGINT       DEFAULT 0,
    leader_name  VARCHAR(50),
    phone        VARCHAR(20),
    sort_order   INT          DEFAULT 0,
    status       TINYINT      NOT NULL DEFAULT 1,
    deleted      TINYINT      NOT NULL DEFAULT 0  COMMENT '逻辑删除',
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 菜单权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    parent_id    BIGINT       DEFAULT 0,
    name         VARCHAR(50)  NOT NULL            COMMENT '菜单名称',
    type         VARCHAR(20)  NOT NULL            COMMENT 'directory=目录, menu=菜单, button=按钮',
    path         VARCHAR(200)                     COMMENT '路由路径',
    component    VARCHAR(200)                     COMMENT '前端组件路径',
    icon         VARCHAR(50)                      COMMENT '图标',
    permission   VARCHAR(100)                     COMMENT '权限标识',
    sort_order   INT          DEFAULT 0,
    visible      TINYINT      DEFAULT 1           COMMENT '是否可见',
    status       TINYINT      DEFAULT 1,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id  BIGINT NOT NULL,
    menu_id  BIGINT NOT NULL,
    UNIQUE INDEX uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ===================================================
-- 第三阶段：日志审计基础能力 - 建表脚本
-- ===================================================

-- 登录日志表
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

-- 操作日志表
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
