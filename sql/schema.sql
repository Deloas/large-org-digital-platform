-- ===================================================
-- 大型组织数字化办公与安全审计一体化平台
-- 全阶段建表脚本（Phase 1-6）
-- 适用于全新环境初始化
-- 执行方式：mysql -u root -p <database> < sql/schema.sql
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

-- ===================================================
-- 第四阶段：采购审批与合同归档 - 建表脚本
-- ===================================================

-- 采购申请表
CREATE TABLE IF NOT EXISTS procurement_request (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    request_no    VARCHAR(30)  NOT NULL UNIQUE COMMENT '申请单号',
    title         VARCHAR(200) NOT NULL COMMENT '采购标题',
    description   TEXT         COMMENT '采购说明',
    amount        DECIMAL(12,2) NOT NULL COMMENT '采购金额',
    category      VARCHAR(50)  COMMENT '采购品类',
    status        VARCHAR(20)  NOT NULL DEFAULT 'draft' COMMENT 'draft/pending/approved/rejected/withdrawn',
    applicant_id  BIGINT       NOT NULL COMMENT '申请人ID',
    dept_id       BIGINT       COMMENT '申请部门ID',
    current_step  INT          NOT NULL DEFAULT 0 COMMENT '当前审批步骤',
    total_steps   INT          NOT NULL DEFAULT 0 COMMENT '总审批步骤数',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_request_no (request_no),
    INDEX idx_status (status),
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_dept_id (dept_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购申请表';

-- 审批记录表
CREATE TABLE IF NOT EXISTS procurement_approval (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    request_id    BIGINT       NOT NULL COMMENT '关联申请ID',
    step_order    INT          NOT NULL COMMENT '审批步骤序号',
    expected_role VARCHAR(50)  NOT NULL COMMENT '预期审批角色',
    approver_id   BIGINT       COMMENT '实际审批人ID',
    status        VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected',
    comment       VARCHAR(500) COMMENT '审批意见',
    approved_at   DATETIME     COMMENT '审批时间',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_request_id (request_id),
    INDEX idx_status (status),
    INDEX idx_expected_role (expected_role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- 供应商表
CREATE TABLE IF NOT EXISTS supplier (
    id             BIGINT       PRIMARY KEY AUTO_INCREMENT,
    supplier_no    VARCHAR(30)  NOT NULL UNIQUE COMMENT '供应商编号',
    name           VARCHAR(200) NOT NULL COMMENT '供应商名称',
    contact_person VARCHAR(50)  COMMENT '联系人',
    contact_phone  VARCHAR(30)  COMMENT '联系电话',
    email          VARCHAR(100) COMMENT '邮箱',
    address        VARCHAR(300) COMMENT '地址',
    qualification  VARCHAR(500) COMMENT '资质描述',
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '1=正常 0=停用',
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_supplier_no (supplier_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 合同表
CREATE TABLE IF NOT EXISTS contract (
    id            BIGINT        PRIMARY KEY AUTO_INCREMENT,
    contract_no   VARCHAR(30)   NOT NULL UNIQUE COMMENT '合同编号',
    request_id    BIGINT        COMMENT '关联采购申请ID',
    supplier_id   BIGINT        COMMENT '关联供应商ID',
    title         VARCHAR(200)  NOT NULL COMMENT '合同标题',
    amount        DECIMAL(12,2) NOT NULL COMMENT '合同金额',
    signed_date   DATE          COMMENT '签订日期',
    expiry_date   DATE          COMMENT '到期日期',
    status        VARCHAR(20)   NOT NULL DEFAULT 'active' COMMENT 'active/completed/terminated',
    created_at    DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_contract_no (contract_no),
    INDEX idx_request_id (request_id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同表';

-- 付款节点表
CREATE TABLE IF NOT EXISTS payment_node (
    id           BIGINT        PRIMARY KEY AUTO_INCREMENT,
    contract_id  BIGINT        NOT NULL COMMENT '关联合同ID',
    node_name    VARCHAR(100)  NOT NULL COMMENT '节点名称',
    amount       DECIMAL(12,2) NOT NULL COMMENT '付款金额',
    ratio        DECIMAL(5,2)  COMMENT '付款比例',
    planned_date DATE          COMMENT '计划付款日期',
    actual_date  DATE          COMMENT '实际付款日期',
    status       VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT 'pending/paid',
    created_at   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_contract_id (contract_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='付款节点表';

-- ===================================================
-- 第五阶段：知识库 RAG 文档问答 - 建表脚本
-- ===================================================

-- 知识库文档表
CREATE TABLE IF NOT EXISTS knowledge_document (
    id             BIGINT       PRIMARY KEY AUTO_INCREMENT,
    title          VARCHAR(255) NOT NULL                  COMMENT '文档标题',
    file_name      VARCHAR(255) NOT NULL                  COMMENT '原始文件名',
    file_type      VARCHAR(20)  NOT NULL                  COMMENT 'PDF / DOCX / TXT',
    file_size      BIGINT                                 COMMENT '文件大小（字节）',
    file_path      VARCHAR(500)                           COMMENT '存储路径',
    content_text   MEDIUMTEXT                             COMMENT '解析后的纯文本内容',
    chunk_count    INT          DEFAULT 0                 COMMENT 'chunk 数量',
    status         VARCHAR(20)  DEFAULT 'processing'      COMMENT 'processing / ready / failed',
    upload_user_id BIGINT                                 COMMENT '上传人ID',
    upload_username VARCHAR(64)                           COMMENT '上传人用户名',
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_upload_user_id (upload_user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 知识库文本块表
CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id          BIGINT    PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT    NOT NULL                  COMMENT '所属文档ID',
    chunk_index INT       NOT NULL                  COMMENT '块序号（从0开始）',
    content     TEXT      NOT NULL                  COMMENT '块文本内容',
    char_count  INT       DEFAULT 0                 COMMENT '字符数',
    embedding   JSON                                COMMENT '特征向量（JSON数组）',
    created_at  DATETIME  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_document_id (document_id),
    INDEX idx_chunk_index (document_id, chunk_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文本块表';

-- 知识库问答日志表
CREATE TABLE IF NOT EXISTS knowledge_qa_log (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL                  COMMENT '提问人ID',
    username    VARCHAR(64)  NOT NULL                  COMMENT '提问人用户名',
    question    TEXT         NOT NULL                  COMMENT '用户问题',
    answer      TEXT                                   COMMENT '系统回答',
    sources     JSON                                   COMMENT '引用来源列表',
    confidence  DECIMAL(5,4)                           COMMENT '置信度',
    status      VARCHAR(20)  DEFAULT 'answered'        COMMENT 'answered / no_match',
    cost_ms     BIGINT                                 COMMENT '耗时（毫秒）',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库问答日志表';

-- ===================================================
-- 第六阶段：安全审计增强 - 建表脚本
-- ===================================================

-- 安全告警表
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

-- IP 黑名单表
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
