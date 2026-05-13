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
